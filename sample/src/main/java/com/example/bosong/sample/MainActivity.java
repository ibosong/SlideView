package com.example.bosong.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.bosong.slideview.SlideView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new MyAdapter(30);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int expandedIndex = mAdapter.isExpanded();
                if(expandedIndex > -1) {
                    mRecyclerView.requestDisallowInterceptTouchEvent(true);
                    mAdapter.collapsedItem(expandedIndex);
                    return true;
                }
                return false;
            }
        });
    }

    private static class MyAdapter extends RecyclerView.Adapter<ViewHolder> {
        RecyclerView mRecyclerView;
        private int mCount;
        private boolean[] stateList; // Expanded -> true

        public MyAdapter(int count) {
            mCount = count;
            stateList = new boolean[count];
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            mRecyclerView = recyclerView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content,parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.slideView.setOnSlideLeftListener(new SlideView.OnSlideListener() {
                @Override
                public void onSlided(boolean expanded) {
                    stateList[position] = expanded;
                }
            });
            if(!stateList[position]) {
                holder.collapsed();
            }
        }

        @Override
        public int getItemCount() {
            return mCount;
        }

        public void collapsedItem(int position) {
            stateList[position] = false;
            notifyItemChanged(position);
        }

        public int isExpanded() {
            for(int i = 0; i < stateList.length; ++i) {
                if(stateList[i]) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public SlideView slideView;
        public ViewHolder(View itemView) {
            super(itemView);
            slideView = (SlideView) itemView.findViewById(R.id.slide_view);
        }

        public void collapsed() {
            slideView.collapse();
        }
    }
}
