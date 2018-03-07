package com.frankenstein.frankenstein;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by davidmena on 2/24/18.
 *
 *
 */

public class CustomListAdapter extends ArrayAdapter<Card>{

    private static final String TAG = "CustomListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;
    private LruCache mMemoryCache;

    //A view Holder for all "View" Items
    private static class ViewHolder{
        TextView caption;
        ImageView image;
    }

    public CustomListAdapter(Context context, int resource, ArrayList<Card> arrayList) {
        super(context, resource, arrayList);
        mContext = context;
        mResource = resource;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    @Override
    public void add(@Nullable Card object) {
        super.add(object);
        String imgUrl = object.getImgURL();
        Bitmap decodedByte;

        //Change bitmap string to Bitmap item
        if (imgUrl != null) {
            byte[] decodedString = Base64.decode(imgUrl, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            mMemoryCache.put(imgUrl, decodedByte);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String caption = getItem(position).getCaption();
        String imgUrl = getItem(position).getImgURL();

        try {
            //ViewHolder Object
            final ViewHolder holder;

            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);
                holder = new ViewHolder();
                holder.caption = (TextView) convertView.findViewById(R.id.cardTitle);
                holder.image = (ImageView) convertView.findViewById(R.id.cardImage);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            lastPosition = position;
            holder.caption.setText(caption);
            holder.image.setImageBitmap((Bitmap) mMemoryCache.get(imgUrl));

            int defaultImage = mContext.getResources().getIdentifier(
                    "@drawable/image_failed",null,mContext.getPackageName());

            //Create Display Options
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .resetViewBeforeLoading(true)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageOnLoading(defaultImage).build();


            return convertView;

        }catch (IllegalArgumentException e){
            Log.e(TAG, "Illegal Argument: " + e.getMessage());
            return convertView;
        }
    }

    public String getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
        return MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
    }


    //This Sets up the Universal Image Loader Library
    private void setupImageLoader(){
        //Universal Image Loader Set up
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(options)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();
        //Start The loader
        ImageLoader.getInstance().init(configuration);
    }
}
