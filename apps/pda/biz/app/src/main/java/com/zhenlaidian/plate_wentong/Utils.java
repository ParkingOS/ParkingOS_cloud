package com.zhenlaidian.plate_wentong;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class Utils {
	/**
	 * Converts YUV420 NV21 to ARGB8888
	 * 
	 * @param data
	 *            byte array on YUV420 NV21 format.
	 * @param width
	 *            pixels width
	 * @param height
	 *            pixels height
	 * @return a ARGB8888 pixels int array. Where each int is a pixels ARGB.
	 */
	public static int[] convertYUV420_NV21toARGB8888(byte[] data, int width, int height) {
		int size = width * height;
		int offset = size;
		int[] pixels = new int[size];
		int u, v, y1, y2, y3, y4;

		// i along Y and the final pixels
		// k along pixels U and V
		for (int i = 0, k = 0; i < size; i += 2, k += 2) {
			y1 = data[i] & 0xff;
			y2 = data[i + 1] & 0xff;
			y3 = data[width + i] & 0xff;
			y4 = data[width + i + 1] & 0xff;

			u = data[offset + k] & 0xff;
			v = data[offset + k + 1] & 0xff;
			u = u - 128;
			v = v - 128;

			pixels[i] = convertYUVtoARGB(y1, u, v);
			pixels[i + 1] = convertYUVtoARGB(y2, u, v);
			pixels[width + i] = convertYUVtoARGB(y3, u, v);
			pixels[width + i + 1] = convertYUVtoARGB(y4, u, v);

			if (i != 0 && (i + 2) % width == 0)
				i += width;
		}

		return pixels;
	}

	private  static int convertYUVtoARGB(int y, int u, int v) {
		int r, g, b;

		r = y + (int) 1.402f * u;
		g = y - (int) (0.344f * v + 0.714f * u);
		b = y + (int) 1.772f * v;
		r = r > 255 ? 255 : r < 0 ? 0 : r;
		g = g > 255 ? 255 : g < 0 ? 0 : g;
		b = b > 255 ? 255 : b < 0 ? 0 : b;
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}
	
    public static int setRotation(int width, int height,int uiRot ,int rotation){
		if(width>=height){
			if(uiRot==1||uiRot==3){
				switch (uiRot)
				{
				case 1:
					rotation = 0;

					break;
				case 3:
					rotation = 180;
					
					break;
				}
				
			}else{
				switch (uiRot)
				{
				case 0:
					rotation = 0;
					
					break;
			
				case 2:
					rotation = 180;
					
					break;
				
				}
			}
		}else if(height>=width){
			if(uiRot==0||uiRot==2){
				
				switch (uiRot) 
				{
				case 0:
					rotation = 90;

					break;
			
				case 2:
					rotation = 270;

					break;
	
				}
			}else{
				switch (uiRot)
				{
				case 1:
					rotation = 270;

					break;			
				case 3:
					rotation = 90;

					break;
				}
		}
		} 
		return rotation;
    }
    @TargetApi(19)  
    public static String getPath(final Context context, final Uri uri) {  
      
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;    
        // DocumentProvider  
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {  
            // ExternalStorageProvider  
            if (isExternalStorageDocument(uri)) {  
                final String docId = DocumentsContract.getDocumentId(uri);  
                final String[] split = docId.split(":");  
                final String type = split[0];  
      
                if ("primary".equalsIgnoreCase(type)) {  
                    return Environment.getExternalStorageDirectory() + "/" + split[1];  
                }  
            }  
            // DownloadsProvider  
            else if (isDownloadsDocument(uri)) {  
      
                final String id = DocumentsContract.getDocumentId(uri);  
                final Uri contentUri = ContentUris.withAppendedId(  
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
      
                return getDataColumn(context, contentUri, null, null);  
            }  
            // MediaProvider  
            else if (isMediaDocument(uri)) {  
                final String docId = DocumentsContract.getDocumentId(uri);  
                final String[] split = docId.split(":");  
                final String type = split[0];  
      
                Uri contentUri = null;  
                if ("image".equals(type)) {  
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
                } else if ("video".equals(type)) {  
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
                } else if ("audio".equals(type)) {  
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
                }  
      
                final String selection = "_id=?";  
                final String[] selectionArgs = new String[] { split[1] };  
      
                return getDataColumn(context, contentUri, selection, selectionArgs);  
            }  
        }  
        // MediaStore (and general)  
        else if ("content".equalsIgnoreCase(uri.getScheme())) {  
      
            // Return the remote address  
            if (isGooglePhotosUri(uri))  
                return uri.getLastPathSegment();  
      
            return getDataColumn(context, uri, null, null);  
        }  
        // File  
        else if ("file".equalsIgnoreCase(uri.getScheme())) {  
            return uri.getPath();  
        }  
      
        return null;  
    }  
    public static String getDataColumn(Context context, Uri uri, String selection,  
            String[] selectionArgs) {  
      
        Cursor cursor = null;  
        final String column = "_data";  
        final String[] projection = { column };  
      
        try {  
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,  
                    null);  
            if (cursor != null && cursor.moveToFirst()) {  
                final int index = cursor.getColumnIndexOrThrow(column);  
                return cursor.getString(index);  
            }  
        } finally {  
            if (cursor != null)  
                cursor.close();  
        }  
        return null;  
    }  
    public static boolean isGooglePhotosUri(Uri uri) {  
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
    }
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is ExternalStorageProvider. 
     */  
    public static boolean isExternalStorageDocument(Uri uri) {  
        return "com.android.externalstorage.documents".equals(uri.getAuthority());  
    }  
      
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is DownloadsProvider. 
     */  
    public static boolean isDownloadsDocument(Uri uri) {  
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
    }  
      
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is MediaProvider. 
     */  
    public static boolean isMediaDocument(Uri uri) {  
        return "com.android.providers.media.documents".equals(uri.getAuthority());  
    }  
}
