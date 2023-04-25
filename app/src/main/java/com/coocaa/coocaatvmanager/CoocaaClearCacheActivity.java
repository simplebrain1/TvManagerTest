package com.coocaa.coocaatvmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coocaa.coocaatvmanager.bean.CacheInfo;
import com.coocaa.coocaatvmanager.utils.CoocaaAppSizeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoocaaClearCacheActivity extends Activity {

	private static String TAG = CoocaaClearCacheActivity.class.getSimpleName();
	protected static final int SCANING = 1;
	public static final int SHOW_CACHE_INFO = 2;
	protected static final int SCAN_FINISH = 3;
	protected static final int CLEAR_FINISH = 4;
	private ProgressBar progressBar1;
	private LinearLayout ll_container;
	private TextView tv_status;
	private PackageManager pm;

	List<CacheInfo> cacheInfos = new ArrayList<CacheInfo>();
	Map<String,View> cacheViewMaps = new HashMap<>();
	Map<String,CacheInfo> cacheInfoMaps = new HashMap<>();
	private Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case SCANING:
					String text = (String) msg.obj;
					tv_status.setText("正在扫描："+text);
					break;
				case SHOW_CACHE_INFO:
					View view = View.inflate(getApplicationContext(), R.layout.list_appcache_item, null);
					ImageView iv = (ImageView) view.findViewById(R.id.iv_icon);
					TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
					TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache);
					final CacheInfo info = (CacheInfo) msg.obj;
					iv.setImageDrawable(info.icon);
					tv_name.setText(info.name);
					tv_cache.setText("缓存大小："+Formatter.formatFileSize(getApplicationContext(), info.size));
					ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
					view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							Log.i(TAG," onFocusChange hasFocus:"+hasFocus);
							if(hasFocus){
								v.setBackgroundColor(getResources().getColor(R.color.gray));
							}else{
								v.setBackgroundColor(getResources().getColor(R.color.purple_500));
							}
						}
					});
					iv_delete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Log.i(TAG," iv_delete onClick ");
							try {
								CoocaaAppSizeUtils.getInstance().clearAppClearCache(CoocaaClearCacheActivity.this, info.packname, new CoocaaAppSizeUtils.IClearAppCacheCallBack() {
									@Override
									public void onRemoveCompleted(String packageName, boolean success) {
										handler.post(new Runnable() {
											@Override
											public void run() {
												Toast.makeText(CoocaaClearCacheActivity.this,"清理应用"+packageName+(success?"缓存成功":"缓存失败"),Toast.LENGTH_SHORT).show();
												updateCacheInfoView(packageName);
											}
										});

									}
								});
							} catch (Exception e) {
								Intent intent = new Intent();
								intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
								intent.addCategory(Intent.CATEGORY_DEFAULT);
								intent.setData(Uri.parse("package:"+info.packname));
								startActivity(intent);
								e.printStackTrace();
							}
						}
					});
					cacheInfos.add(info);
					cacheInfoMaps.put(info.packname,info);
					cacheViewMaps.put(info.packname,view);
					ll_container.addView(view, 0);
					break;
				case SCAN_FINISH:
					tv_status.setText("扫描完毕");
					break;
				case CLEAR_FINISH:
					tv_status.setText("清理完成");
					break;
			}
		};
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		ll_container = (LinearLayout) findViewById(R.id.ll_container);
		tv_status = (TextView) findViewById(R.id.tv_status);

		new Thread() {
			public void run() {
				pm = getPackageManager();
				List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
				progressBar1.setMax(packageInfos.size());
				int total = 0;
				for(PackageInfo packinfo: packageInfos){
					try {
						String packname = packinfo.packageName;
						CoocaaAppSizeUtils.getInstance().getSingleAppSize(CoocaaClearCacheActivity.this,packinfo.packageName,new CoocaaAppSizeUtils.OnBackListent() {
							@Override
							public void backData(long cacheSize, long dataSize, long codeSize,long cacheQuotaBytes) {
								long cache = cacheSize;
								if(cache>0){
									try {
										Message msg = Message.obtain();
										msg.what = SHOW_CACHE_INFO;
										CacheInfo cacheInfo = new CacheInfo();
										cacheInfo.packname = packname;
										cacheInfo.icon = pm.getApplicationInfo(packname, 0).loadIcon(pm);
										cacheInfo.name = pm.getApplicationInfo(packname, 0).loadLabel(pm).toString();
										cacheInfo.size = cache;
										msg.obj = cacheInfo;
										handler.sendMessage(msg);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						});
						Message msg = Message.obtain();
						msg.what= SCANING;
						msg.obj = packinfo.applicationInfo.loadLabel(pm).toString();
						handler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
					total ++;
					progressBar1.setProgress(total);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Message msg = Message.obtain();
				msg.what = SCAN_FINISH;
				handler.sendMessage(msg);
			};
		}.start();

	}

	int total = 0;
	public void cleanAll(View view){
//		/freeStorageAndNotify
		Log.i(TAG," cleanAll");
//		Method[] methods = PackageManager.class.getMethods();
//		for(Method method:methods){
//			if("freeStorageAndNotify".equals(method.getName())){
//				try {
//					CoocaaStorageUtil.StorageEntity storageEntity = CoocaaStorageUtil.queryWithStorageManager(this);
//					long freeSize = storageEntity.freeSize;
//					method.invoke(pm, freeSize, new IPackageDataObserver.Stub() {
//						@Override
//						public void onRemoveCompleted(String packageName,
//								boolean succeeded) throws RemoteException {
//							Log.i(TAG," cleanAll onRemoveCompleted succeeded:"+succeeded+",packageName:"+packageName);
//							System.out.println(succeeded);
//						}
//					});
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return;
//			}
//		}
		if(cacheInfos!=null){
			total = 0;
			for (CacheInfo cacheInfo : cacheInfos){
				CoocaaAppSizeUtils.getInstance().clearAppClearCache(this, cacheInfo.packname, new CoocaaAppSizeUtils.IClearAppCacheCallBack() {
					@Override
					public void onRemoveCompleted(String packageName, boolean success) {
						Log.i(TAG," cleanAll onRemoveCompleted succeeded:"+success+",packageName:"+packageName+" ,total："+total);
						handler.post(new Runnable() {
							@Override
							public void run() {
								total ++;
								progressBar1.setProgress(total);
								tv_status.setText("清理应用"+packageName+(success?"缓存成功":"缓存失败"));
								updateCacheInfoView(packageName);
							}
						});

						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(total==cacheInfo.size-2) {
							handler.sendEmptyMessage(CLEAR_FINISH);
						}

					}

				});
			}
		}
	}

	public void updateCacheInfoView(String pkgName){
		if(cacheViewMaps!=null&&cacheViewMaps.size()>0){
			if(cacheViewMaps.containsKey(pkgName)){
				View view = cacheViewMaps.get(pkgName);
				CoocaaAppSizeUtils.getInstance().getSingleAppSize(this, pkgName, new CoocaaAppSizeUtils.OnBackListent() {
					@Override
					public void backData(long cacheSize, long dataSize, long codeSize,long cacheQuotaBytes) {
						ImageView iv = (ImageView) view.findViewById(R.id.iv_icon);
						TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
						TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache);
						tv_cache.setText("缓存大小："+Formatter.formatFileSize(getApplicationContext(),cacheSize));
					}
				});
			}
		}
	}

}