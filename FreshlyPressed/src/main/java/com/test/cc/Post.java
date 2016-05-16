package com.test.cc;

import android.net.Uri;

import org.json.JSONObject;

public class Post {

    private String mTitle;

    private String mExcerpt;

    private Uri mUri;

    private Uri mFeaturedImage;

    private final JSONObject mJson;

    public static Post build(JSONObject json) {
        return new Post(json);
    }

    public Post(JSONObject json) {
        mJson = json;
        mTitle = mJson.optString("title");
        mExcerpt = mJson.optString("excerpt");
        mUri = Uri.parse(mJson.optString("URL"));
        String urlString = mJson.optString("featured_image");
        if (urlString != null && !urlString.isEmpty()) {
            Uri.Builder b = new Uri.Builder();
            b.encodedPath(urlString);
            mFeaturedImage = b.build();
        }
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getExcerpt() {
        return mExcerpt;
    }

    public Uri getUri() {
        return mUri;
    }

    public Uri getFeaturedImageUrl() {
        return mFeaturedImage;
    }

}