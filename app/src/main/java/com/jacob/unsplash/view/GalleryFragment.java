package com.jacob.unsplash.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacob.unsplash.DetailActivity;
import com.jacob.unsplash.R;
import com.jacob.unsplash.api.UnSplashRepository;
import com.jacob.unsplash.model.Photo;
import com.jacob.unsplash.view.adapter.PhotoAdapter;

import java.util.List;


/**
 * Created by vynnykiakiv on 3/23/18.
 */

public class GalleryFragment extends Fragment
        implements PhotoAdapter.OnItemClickListener,
        UnSplashRepository.Listener {

    public static final String TAG = GalleryFragment.class.getName();
    private static final int COLUMNS = 2;
    private PhotoAdapter mAdapter;

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), COLUMNS);
        recyclerView.setLayoutManager(manager);
        mAdapter = new PhotoAdapter(getActivity());
        mAdapter.setOnCLickListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSuccess(List<Photo> photos) {
        if (mAdapter != null && photos.size() > 0) {
            mAdapter.setList(photos);
        } else {
            onFail(getString(R.string.error_nothing_found));
        }
    }

    @Override
    public void onFail(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onItemClicked(Photo photo) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(ItemViewFragment.ARG_PHOTO, photo);
        getActivity().startActivity(intent);
    }
}