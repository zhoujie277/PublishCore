package com.mysoft.core.util;

import android.app.Activity;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.mysoft.core.L;
import com.mysoft.core.MApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay
 */
public class NewSdcardUtils {

	private static final String TAG = NewSdcardUtils.class.getSimpleName();

	// 旋风下载SD卡预留5MB空间不能用
	private static final long QQDOWNLOADER_LEFT_SD_SIZE = 5 * 1024 * 1024;

	// SD卡列表
	private static List<SdcardInfo> sSdcardList = new ArrayList<SdcardInfo>();
	private static String sSdcardPath = null;

	public static synchronized void init() {
		init(false);
	}

	public static synchronized void init(boolean forceInit) {
		// NOTE: 4.0(14)+才有隐藏的getVolumeList,getVolumeState等api
		initSdcardPath(); // 使用系统隐藏api的方式
	}

	/**
	 * 判断是否有sd卡（内外置都可以）
	 * 
	 * @return
	 */
	public static boolean hasSdcard() {
		String sdcard = getInternalSdcardPath(false);
		if (TextUtils.isEmpty(sdcard)) {
			sdcard = getExternalSdcardPath(true);
		}
		return !TextUtils.isEmpty(sdcard);
	}

	/**
	 * 获取内置SD卡挂载路径
	 * 
	 * @param useFallback:
	 *            获取不到的时候，使用系统的API
	 * @return
	 */
	public static synchronized String getInternalSdcardPath(boolean useFallback) {
		return getInternalSdcardPath(true, useFallback);
	}

	/**
	 * 获取外置SD卡挂载路径
	 * 
	 * @param useFallback:
	 *            获取不到的时候，使用系统的API
	 * @return
	 */
	public static synchronized String getExternalSdcardPath(boolean useFallback) {
		return getExternalSdcardPath(true, useFallback);
	}

	/**
	 * 获取内置SD卡挂载路径，特殊情况使用（例如：扫描ROM，只要求SD卡是可读的）
	 * 
	 * @param needWritable:
	 *            是否要求sd卡是可写的
	 * @param useFallback:
	 *            获取不到的时候，使用系统的API
	 * @return
	 */
	public static synchronized String getInternalSdcardPath(boolean needWritable, boolean useFallback) {
		return getSdcardPath(true, needWritable, useFallback);
	}

	/**
	 * 获取外置SD卡挂载路径，特殊情况使用（例如：扫描ROM，只要求SD卡是可读的）
	 * 
	 * @param needWritable:
	 *            是否要求sd卡是可写的
	 * @param useFallback:
	 *            获取不到的时候，使用系统的API
	 * @return
	 */
	public static synchronized String getExternalSdcardPath(boolean needWritable, boolean useFallback) {
		return getSdcardPath(false, needWritable, useFallback);
	}

	/**
	 * 初始化检测SD卡
	 */
	private static void initSdcardPath() {
		long startTime = System.currentTimeMillis();
		L.i(TAG, "initSdcardPath start");
		sSdcardList.clear();
		try {
			StorageManager storageManager = (StorageManager) MApplication.getApplication()
					.getSystemService(Activity.STORAGE_SERVICE);
			Method getVolumeStateMethod = storageManager.getClass().getMethod("getVolumeState", String.class);
			Method getVolumeListMethod = storageManager.getClass().getMethod("getVolumeList");
			Object[] volumes = (Object[]) getVolumeListMethod.invoke(storageManager);
			for (int i = 0; i < volumes.length; i++) {
				Method getPathMethod = volumes[i].getClass().getMethod("getPath");
				// volumes[i].getClass().getMethod("isPrimary");
				Method isRemovableMethod = volumes[i].getClass().getMethod("isRemovable");
				Method isEmulatedMethod = volumes[i].getClass().getMethod("isEmulated");
				String path = (String) getPathMethod.invoke(volumes[i]);
				String state = (String) getVolumeStateMethod.invoke(storageManager, path);
				boolean isRemovable = (Boolean) isRemovableMethod.invoke(volumes[i]);
				boolean isEmulated = (Boolean) isEmulatedMethod.invoke(volumes[i]);
				L.v(TAG, "initSdcardPath path:" + path + ";state:" + state + ";isRemovable:" + isRemovable
						+ ";isEmulated:" + isEmulated);
				// 挂载状态
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					SdcardInfo sdcardInfo = new SdcardInfo();
					sdcardInfo.mountPoint = path;
					sdcardInfo.isInternal = !isRemovable; // 如果是removable的则认为是外置sd卡，待验证
					// NOTE:
					// 4.4以上，WRITE_MEDIA_STORAGE只有内置应用才能授权，外部SD卡只读的
					sdcardInfo.isWritable = isWritable(new File(path));
					sdcardInfo.availableSize = calculateAvailableSize(path);
					sdcardInfo.totalSize = calculateSize(path);
					sSdcardList.add(sdcardInfo);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		SdcardInfo maxAvaliableSdcard = null;
		for (SdcardInfo sdcardInfo : sSdcardList) {
			L.i(TAG, "initSdcardPath sdcard:" + sdcardInfo);
			if (sdcardInfo.isWritable) {
				if (maxAvaliableSdcard == null || maxAvaliableSdcard.availableSize < sdcardInfo.availableSize) {
					maxAvaliableSdcard = sdcardInfo;
				}
			}
		}
		if (maxAvaliableSdcard != null) {
			sSdcardPath = maxAvaliableSdcard.mountPoint;
		} else {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				sSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			}
		}
		L.i(TAG, "initSdcardPath sSdcardPath:" + sSdcardPath);
		long endTime = System.currentTimeMillis();
		L.i(TAG, "initSdcardPath used time:" + (endTime - startTime) + "ms");
	}

	public static String getMaxAvaliableSdcardPath() {
		return sSdcardPath;
	}
	
	@SuppressWarnings("deprecation")
	private static long calculateSize(String sdcardPath) {
		StatFs sf = new StatFs(sdcardPath);
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long size = blockSize * blockCount;
		L.i(TAG, "getSdcardSize sdcardPath:" + sdcardPath + ";blockSize:" + blockSize + ";blockCount:" + blockCount);
		if (size < 0) {
			size = 0;
		}
		return size;
	}

	@SuppressWarnings("deprecation")
	public static long calculateAvailableSize(String sdcardPath) {
		StatFs sf = new StatFs(sdcardPath);
		long blockSize = sf.getBlockSize();
		long availableBlockCount = sf.getAvailableBlocks();
		L.i(TAG, "SdcardAvailableSize sdcardPath:" + sdcardPath + ";blockSize:" + blockSize + ";availableBlockCount:"
				+ availableBlockCount);
		long size = blockSize * availableBlockCount;
		if (size < 0) {
			size = 0;
		}
		return size;
	}

	private static boolean isWritable(File dir) {
		boolean isWritable = false;

		// NOTE: 三星N900，出现外置SD卡无法读写的情况（shell帐号可以），通过使用创建文件的方式加强检测
		if (dir.exists() && dir.canWrite()) {
			File tmpFile = new File(dir, ".shuame-mobile-writable-" + System.currentTimeMillis());
			if (!tmpFile.exists()) {
				try {
					isWritable = tmpFile.createNewFile();
					boolean isDeleteOk = tmpFile.delete();
					L.i(TAG, "isWritable:" + isWritable + ";tmpFile:" + tmpFile + ";isDeleteOk:" + isDeleteOk);
				} catch (IOException e) {
					e.printStackTrace();
					L.i(TAG, "isWritable:" + isWritable + ";tmpFile:" + tmpFile + ";IOException:" + e);
				}
			} else {
				isWritable = true;
			}
		}
		L.i(TAG, "isWritable:" + isWritable + ";dir:" + dir);

		return isWritable;
	}

	/**
	 * 获取SD卡挂载路径
	 * 
	 * @param isInternal
	 * @param useFallback:
	 *            获取不到的时候，使用系统的API
	 * @return
	 */
	private static String getSdcardPath(boolean isInternal, boolean needWritable, boolean useFallback) {
		String sdcardPath = null;
		for (SdcardInfo sdcardInfo : sSdcardList) {
			if (isInternal && sdcardInfo.isInternal) {
				if (needWritable) { // 需要是可写的
					if (sdcardInfo.isWritable) {
						sdcardPath = sdcardInfo.mountPoint;
					}
				} else { // 不需要是可写的
					sdcardPath = sdcardInfo.mountPoint;
				}
				break;
			} else if (!isInternal && !sdcardInfo.isInternal) {
				if (needWritable) { // 需要是可写的
					if (sdcardInfo.isWritable) {
						sdcardPath = sdcardInfo.mountPoint;
					}
				} else { // 不需要是可写的
					sdcardPath = sdcardInfo.mountPoint;
				}
				break;
			}
		}

		L.i(TAG, "getSdcardPath sdcardPath:" + sdcardPath + ";isInternal:" + isInternal + ";useFallback:"
				+ useFallback);

		// 使用系统的API获取的sd卡路径补救
		if (useFallback && TextUtils.isEmpty(sdcardPath)) {
			L.i(TAG, "getSdcardPath Use system api to get sdcard path isInternal:" + isInternal);
			sdcardPath = getSystemSdcardPath();
		}

		return sdcardPath;
	}

	private static String getSystemSdcardPath() {
		String sdcardPath = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
			L.i(TAG, "getSystemSdcardPath Use system api sdcardPath:" + sdcardPath);
		}
		return sdcardPath;
	}

	/**
	 * 通过文件路径获取所在SD卡的空间
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getSdcardSize(String filePath) {
		long size = -1;
		if (!TextUtils.isEmpty(filePath)) {
			String internalSdcardPath = getInternalSdcardPath(false);
			String externalSdcardPath = getExternalSdcardPath(false);
			String sdcardPath = getSystemSdcardPath();
			if (!TextUtils.isEmpty(internalSdcardPath) && filePath.startsWith(internalSdcardPath)) {
				sdcardPath = internalSdcardPath;
			} else if (!TextUtils.isEmpty(externalSdcardPath) && filePath.startsWith(externalSdcardPath)) {
				sdcardPath = externalSdcardPath;
			}
			if (!TextUtils.isEmpty(sdcardPath)) {
				try {
					StatFs sf = new StatFs(sdcardPath);
					long blockSize = sf.getBlockSize();
					long blockCount = sf.getBlockCount();
					size = blockSize * blockCount;
					L.i(TAG, "getSdcardSize filePath:" + filePath + ";blockSize:" + blockSize + ";blockCount:"
							+ blockCount);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return size;
	}

	/**
	 * 通过文件路径获取所在SD卡的空间
	 * 
	 * @param filePath
	 * @return
	 */
	public static long getSdcardAvailableSize(String filePath) {
		L.i(TAG, "getSdcardAvailableSize start");
		long size = -1;
		if (!TextUtils.isEmpty(filePath)) {
			String internalSdcardPath = getInternalSdcardPath(false);
			String externalSdcardPath = getExternalSdcardPath(false);
			String sdcardPath = getSystemSdcardPath();

			// e.g. internal: /mnt/sdcard, external: /mnt/sdcard/external_sd
			// e.g. internal: /mnt/sdcard, external: /mnt/sdcard2
			// e.g. internal: /mnt/sdcard, external: /mnt/sdcard/sdcard2
			String sdcardPath1 = internalSdcardPath;
			String sdcardPath2 = externalSdcardPath;
			if (!TextUtils.isEmpty(sdcardPath1) && !TextUtils.isEmpty(sdcardPath2)) {
				if (sdcardPath2.length() > sdcardPath1.length()) {
					String tmp = sdcardPath1;
					sdcardPath1 = sdcardPath2;
					sdcardPath2 = tmp;
				}
			}

			if (!TextUtils.isEmpty(sdcardPath1) && filePath.startsWith(sdcardPath1)) {
				sdcardPath = sdcardPath1;
			} else if (!TextUtils.isEmpty(sdcardPath2) && filePath.startsWith(sdcardPath2)) {
				sdcardPath = sdcardPath2;
			}

			L.i(TAG, "SdcardAvailableSize sdcardPath:" + sdcardPath + ";sdcardPath1:" + sdcardPath1 + ";sdcardPath2:"
					+ sdcardPath2);

			if (!TextUtils.isEmpty(sdcardPath)) {
				try {
					StatFs sf = new StatFs(sdcardPath);
					long blockSize = sf.getBlockSize();
					long availableBlockCount = sf.getAvailableBlocks();
					L.i(TAG, "SdcardAvailableSize sdcardPath:" + sdcardPath + ";blockSize:" + blockSize
							+ ";availableBlockCount:" + availableBlockCount);
					size = blockSize * availableBlockCount - QQDOWNLOADER_LEFT_SD_SIZE;
					if (size < 0) {
						size = 0;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		L.i(TAG, "getSdcardAvailableSize end filePath:" + filePath + ";size:" + size);
		return size;
	}

	public static boolean isSpaceEnough(long size) {
		return !TextUtils.isEmpty(getSdcardPathForSize(size));
	}

	public static boolean isSpaceEnough(String path, long size) {
		return getSdcardAvailableSize(path) > size;
	}

	public static String getSdcardPathForSize(long size) {
		String externalSdcardPath = getExternalSdcardPath(false); // 外置卡
		if (!TextUtils.isEmpty(externalSdcardPath)) {
			if (getSdcardAvailableSize(externalSdcardPath) > size) {
				return externalSdcardPath;
			}
		}

		String internalSdcardPath = NewSdcardUtils.getInternalSdcardPath(false); // 内置卡
		if (!TextUtils.isEmpty(internalSdcardPath)) {
			if (getSdcardAvailableSize(internalSdcardPath) > size) {
				return internalSdcardPath;
			}
		}

		// NOTE: 双SD卡检查获取不到时，使用系统api获取
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
			if (getSdcardAvailableSize(sdDir) > size) {
				return internalSdcardPath;
			}
		}

		return null;
	}

	private static class SdcardInfo {
		public String mountPoint;
		public boolean isInternal;
		// NOTE:
		// 4.4以上，WRITE_MEDIA_STORAGE只有内置应用才能授权，外部SD卡只读的
		public boolean isWritable;
		public long availableSize;
		public long totalSize;

		@Override
		public String toString() {
			String totalSizeString = Formatter.formatFileSize(MApplication.getApplication(), totalSize);
			String availableSizeString = Formatter.formatFileSize(MApplication.getApplication(), availableSize);
			return "SdcardInfo [mountPoint=" + mountPoint + ", isInternal=" + isInternal + ", isWritable=" + isWritable
					+ ", availableSize=" + availableSizeString + ", totalSize=" + totalSizeString + "]";
		}

	}
}
