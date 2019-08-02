package com.magcomm.factorytest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.entity.TestResultEntity;
import com.magcomm.factorytest.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangziran on 2018/1/19.
 */

public class TestResultAdapter extends RecyclerView.Adapter<TestResultAdapter.TestResultViewHolder> {
    private List<TestResultEntity> resultEntities;
    private Context context;
    private LayoutInflater layoutInflater;

    public TestResultAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        resultEntities = new ArrayList<>();
    }

    public void notify(List<TestResultEntity> resultEntities) {
        this.resultEntities = resultEntities;
        notifyDataSetChanged();
    }


    @Override
    public TestResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.test_result_item, null, false);
        TestResultViewHolder viewHolder = new TestResultViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TestResultViewHolder holder, int position) {
        holder.tvItem.setText(resultEntities.get(position).getItem());
        String result = resultEntities.get(position).getResult();
        switch (result) {
            case Config.FAIL:
                holder.tvResult.setTextColor(Color.parseColor("#ddff0000"));
                break;
            case Config.NOT_TEST:
                holder.tvResult.setTextColor(Color.BLUE);
                break;
            case Config.SUCCESS:
                holder.tvResult.setTextColor(Color.GREEN);
                break;
        }
        holder.tvResult.setText(result);
    }

    @Override
    public int getItemCount() {
        return resultEntities.size();
    }

    @Override
    public int getItemViewType(int position) {

        return resultEntities.get(position).getMode();
    }

    public class TestResultViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem, tvResult;

        public TestResultViewHolder(View itemView) {
            super(itemView);
            tvItem = (TextView) itemView.findViewById(R.id.tv_test_tesult_item);
            tvResult = (TextView) itemView.findViewById(R.id.tv_test_tesult_result);
        }
    }

}
