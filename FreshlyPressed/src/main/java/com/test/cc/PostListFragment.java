package com.test.cc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PostListFragment extends SwipeRefreshListFragment
        implements AdapterView.OnItemClickListener {

    private final static String TAG = "PostListFragment";

    private AsyncTask refreshPostsTask;
    private PostsFactory postsFactory;

    // empty constructor necessary for fragments
    public PostListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postsFactory = new PostsFactory(getActivity());
        getListView().setOnItemClickListener(this);
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postsFactory.reset();
                refreshPosts();
            }
        });
        refreshPosts();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        PostsAdapter adapter = (PostsAdapter) getListAdapter();
        Post post = adapter.getItem(position);

        Intent browseIntent = new Intent();
        browseIntent.setAction(Intent.ACTION_VIEW);
        browseIntent.setData(post.getUri());

        getActivity().startActivity(browseIntent);

    }

    protected void notifyDataChanged() {
        if (postsFactory.getPostCount() == 0) {
            setEmptyText(getString(R.string.unavailable_post_list));
        }

        setListAdapter(new PostsAdapter());
        setRefreshing(false);
    }

    /**
     *Solicita la actualización de los pots, solicitandolos a la factory
     * Notifica al adapter cuando se completa la tarea o habilita los mensaje de error necesarios
     * en caso de error.
     */
    private class UpdatePostsTask extends AsyncTask<Void,Void,Exception> {

        @Override
        protected Exception doInBackground(Void... voidParam) {
            try {
                postsFactory.updatePosts();
                return null;
            } catch (Exception e) {
                Log.d(TAG, "Error obteniendo posts", e);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exception) {
            if(exception!=null) {
                setRefreshing(false);
                setListShown(true);
                setEmptyText(getString(R.string.post_list_error));
                return;
            }
            notifyDataChanged();

        }

    }

    /**
     * solicita la actualización de posts, en forma asincrónica
     */
    public void refreshPosts() {
        refreshPostsTask = new UpdatePostsTask().execute();
    }

    private class PostsAdapter extends BaseAdapter {

        private final static int IMAGE_BUFFER_SIZE = 6;

        private int lastPosition = 0;

        @Override
        public Post getItem(int position) {
            return postsFactory.getPostObject(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Post post = getItem(position);
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.post_list_fragment_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView summary = (TextView) convertView.findViewById(R.id.summary);
            title.setText(post.getTitle());
            summary.setText(Html.fromHtml(post.getExcerpt().toString()));

            if (post.getFeaturedImageUrl() != null) {
                loadFeaturedImage(position, convertView);
            } else {
                View featuredImageContainer = convertView.findViewById(R.id.featured_image_container);
                featuredImageContainer.setVisibility(View.GONE);
            }

            if (position >= lastPosition) {
                for (int i = position + 1; i < position + IMAGE_BUFFER_SIZE && i < getCount(); i++) {
                    postsFactory.preloadPostImages(i);
                }
            } else {
                for (int i = position - 1; i > position - IMAGE_BUFFER_SIZE && i > -1; i--) {
                    postsFactory.preloadPostImages(i);
                }
            }
            lastPosition = position;
            return convertView;
        }

        private void loadFeaturedImage(int position, final View itemView) {

            final ImageView featuredImage = (ImageView) itemView.findViewById(R.id.image);
            final View loading = itemView.findViewById(R.id.progress_bar);
            final View featuredImageContainer = itemView.findViewById(R.id.featured_image_container);
            final View errorText = itemView.findViewById(R.id.error_text);
            errorText.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            featuredImageContainer.setVisibility(View.VISIBLE);
            featuredImage.setVisibility(View.GONE);

            postsFactory.loadPostFeaturedImages(position,featuredImage, new com.squareup.picasso.Callback() {

                        @Override
                        public void onSuccess() {
                            featuredImage.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            loading.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                        }

                    });

        }

        @Override
        public long getItemId(int position) {
            return -1L;
        }

        @Override
        public int getCount() {
            return postsFactory.getPostCount();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postsFactory.clear();
        refreshPostsTask.cancel(true);
    }

}