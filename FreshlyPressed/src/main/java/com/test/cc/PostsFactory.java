package com.test.cc;

import android.content.Context;
import android.net.Uri;
import android.util.LruCache;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Provee los posts que recupera del servicio remoto
 * Además maneja la cache de pedidos.
 * También permite precargar las imágenes de cada uno
 * Created by cristian on 16/05/16.
 */
public class PostsFactory {

    private final static String SERVICE_URL = "https://public-api.wordpress.com/rest/v1/freshly-pressed?number=30";
    private final static int POST_CACHE_SIZE = 64;
    private final static int IMAGE_CACHE_BYTES_SIZE = 100000000;
    private String serviceUrl = SERVICE_URL;

    private JSONArray jsonArray;
    private Picasso mPicasso;
    private LruCache<Integer,Post> mPostsCache;
    private OkHttpClient mClient;
    private Context context;

    private void init() {
        mPicasso = new Picasso.Builder(context)
                .memoryCache(new com.squareup.picasso.LruCache(IMAGE_CACHE_BYTES_SIZE))
                .build();
        mPicasso.setIndicatorsEnabled(true);
        mPicasso.setLoggingEnabled(true);
        mPostsCache = new LruCache<Integer, Post>(POST_CACHE_SIZE);
        mClient = new OkHttpClient();
    }

    public PostsFactory(Context context) {
        this.context = context;
        init();
    }

    /**
     * Obtiene los posts del servicio remoto y los almacena en memoria
     * @throws JSONException si no se puede parsear el json recibido
     * @throws IOException si se produce algún error de red
     */
    public void updatePosts() throws JSONException, IOException {
        Request request = new Request.Builder()
                .url(serviceUrl)
                .build();
        Response response = mClient.newCall(request).execute();
        JSONObject json = new JSONObject(response.body().string());
        jsonArray = json.getJSONArray("posts");
    }

    /**
     * reinicia la factory, limpiando todas las caches y cancelando cualquier pedido en proceso
     */
    public void reset() {
        mClient.dispatcher().cancelAll();
        mPostsCache.evictAll();
        mPicasso.shutdown();
        init();
    }

    /**
     * precarga asincronicamente en cache la imagen del post ubicado en la posición position solicitándola al servicio remoto.
     * @param position
     */
    public void preloadPostImages(int position) {
        Post preloadedPost = getPostObject(position);
        if (preloadedPost.getFeaturedImageUrl() != null) {
            mPicasso.load(preloadedPost.getFeaturedImageUrl().getPath());
        }
    }

    /**
     * Carga asincrónicamente la imagen del post ubicado en la posición position, en la view featuredImageContainerView
     * Además ejecuta la tarea especificada en el listener responseListener
     * @param position
     * @param featuredImageContainerView
     * @param responseListener
     */
    public void loadPostFeaturedImages(int position, ImageView featuredImageContainerView,com.squareup.picasso.Callback responseListener) {
        Post post = getPostObject(position);
        Uri featuredImageUri = post.getFeaturedImageUrl();
        if(featuredImageUri!=null) {
            mPicasso.load(featuredImageUri.getPath())
                    .into(featuredImageContainerView,responseListener);
        }
    }

    /**
     * Obtiene el post ubicado en la posición position
     * @param position
     * @return el post solicitado. Si no existe en memoria o en cache, retorna null.
     */
    public Post getPostObject(int position) {

        Post post = mPostsCache.get(position);
        if (post == null) {
            if(jsonArray != null) {
                post = Post.build(jsonArray.optJSONObject(position));
            }
            if(post!= null) {
                mPostsCache.put(position, post);
            }
        }
        return post;
    }

    /**
     * Devuelve la cantidad de posts en memoria.
     * @return
     */
    public int getPostCount() {
        if(jsonArray==null) {
            return 0;
        }
        return jsonArray.length();
    }

    /**
     * Limpia la factory, liberando los recursos utilizados.
     */
    public void clear() {
        mPicasso.shutdown();
        mClient.dispatcher().cancelAll();
        mPostsCache.evictAll();
    }

}
