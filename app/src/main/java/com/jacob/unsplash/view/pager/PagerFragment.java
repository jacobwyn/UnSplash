package com.jacob.unsplash.view.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jacob.unsplash.PagerActivity;
import com.jacob.unsplash.R;
import com.jacob.unsplash.model.Photo;
import com.jacob.unsplash.utils.DepthPageTransformer;
import com.jacob.unsplash.utils.PermissionHelper;
import com.jacob.unsplash.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PagerFragment extends Fragment implements PagerContract.View, View.OnClickListener, PagerActivity.CurrentItemProvider, OnDragListener {
    private static final String TYPE_TEXT_PLAIN = "text/plain";
    private static final String ARG_START_POSITION = "ARG_START_POSITION";
    private static final String ARG_FIRST_VISIBLE_POSITION = "ARG_FIRST_VISIBLE_POSITION";
    private static final String ARG_LAST_VISIBLE_POSITION = "ARG_LAST_VISIBLE_POSITION";

    private PagerContract.Presenter mPresenter;
    private AppCompatActivity mActivity;
    private ViewPager pager;
    private int startPosition;
    private int lastPosition;
    private int firstPosition;
    private View controls;

    public static PagerFragment newInstance(int startPosition, int firstVisible, int lastVisible) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_START_POSITION, startPosition);
        bundle.putInt(ARG_FIRST_VISIBLE_POSITION, firstVisible);
        bundle.putInt(ARG_LAST_VISIBLE_POSITION, lastVisible);

        PagerFragment fragment = new PagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            mActivity = (AppCompatActivity) context;
        }
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if (args != null) {
            startPosition = args.getInt(ARG_START_POSITION);
            firstPosition = args.getInt(ARG_FIRST_VISIBLE_POSITION);
            lastPosition = args.getInt(ARG_LAST_VISIBLE_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.getBackground().setAlpha(255);
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.item_download_image_view).setOnClickListener(this);
        view.findViewById(R.id.item_share_image_view).setOnClickListener(this);
        pager = (ViewPager) view.findViewById(R.id.view_pager);
        pager.setPageTransformer(true, new DepthPageTransformer());
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPresenter.setCurrentPos(position);
                getToolbar().show();
                showControls(true);
            }
        });
        mPresenter.start();
        controls = view.findViewById(R.id.item_controls_layout);
    }

    @Override
    public void setData(List<Photo> photoList, int currentPos) {
        pager.setAdapter(new PhotoPagerAdapter(getChildFragmentManager(), photoList));
        pager.setCurrentItem(currentPos);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_share_image_view: {
                onShareClicked();
                break;
            }
            case R.id.item_download_image_view: {
                askPermissionAndDownload();
                break;
            }
        }
    }

    private void askPermissionAndDownload() {
        if (PermissionHelper.hasPermission(mActivity)) {
            onDownloadClicked();
        } else {
            PermissionHelper.requestPermission(mActivity);
        }
    }

    private void onShareClicked() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(TYPE_TEXT_PLAIN);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPresenter.getCurrentUrl());
        startActivity(Intent.createChooser(shareIntent, mActivity.getString(R.string.share_picture)));
    }

    private void onDownloadClicked() {
        if (mPresenter != null) {
            mPresenter.onDownloadClicked();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionHelper.hasPermission(mActivity)) {
            onDownloadClicked();
        } else {
            Snackbar.make(getView(), R.string.error_need_permission, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setPresenter(PagerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showMessage(int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveSuccess(File file) {
        Utils.notifyMediaScanner(file, mActivity);
        showMessage(R.string.success_save);
    }

    @Override
    public void onDestroyView() {
        mActivity = null;
        if (mPresenter != null) {
            mPresenter.release();
        }
        super.onDestroyView();
    }

    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        View sharedView = null;
        List<Fragment> fragments = getChildFragmentManager().getFragments();

        int currentItem = getCurrentItem();
        for (Fragment frag : fragments) {
            if (frag instanceof PhotoFragment) {
                PhotoFragment photoFragment = (PhotoFragment) frag;
                if (photoFragment.getCurrent() == currentItem) {
                    sharedView = photoFragment.getView().findViewById(R.id.picture_image_view);
                }
            }
        }
        if (firstPosition <= currentItem && currentItem <= lastPosition) {
            if (startPosition != currentItem) {
                if (sharedView != null) {
                    String transitionName = ViewCompat.getTransitionName(sharedView);
                    names.clear();
                    names.add(transitionName);

                    sharedElements.clear();
                    sharedElements.put(transitionName, sharedView);
                }
            }
        } else {
            names.clear();
            sharedElements.clear();
        }

    }

    @Override
    public int getCurrentItem() {
        return pager.getCurrentItem();
    }

    public int getStartPosition() {
        return startPosition;
    }

    public ActionBar getToolbar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void finishWithAnim() {
        if (getActivity() != null) {
            getActivity().supportFinishAfterTransition();
        }
    }

    @Override
    public void onStartDrag() {
        getToolbar().hide();
        showControls(false);
    }

    @Override
    public void onDragUpdate(float newTopLeftY) {
        if (newTopLeftY > -256 && newTopLeftY < 256) {
            int alpha = (int) (255 - Math.abs(newTopLeftY));
            if (getView() != null) {
                getView().getBackground().setAlpha(alpha);
            }
        }
    }

    @Override
    public boolean onStopDrag(float newTopLeftY) {
        if (newTopLeftY < -256 || newTopLeftY > 256) {
            finishWithAnim();
            return true;
        } else {
            getToolbar().show();
            showControls(true);
            return false;
        }
    }

    private void showControls(boolean show) {
        if (controls != null) {
            controls.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
