package com.mx.gillustrated.adapter;

import java.util.List;

import com.mx.gillustrated.R;
import com.mx.gillustrated.vo.CardTypeInfo;
import com.mx.gillustrated.vo.GameInfo;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardTypeListAdapter extends BaseAdapter {

    private Context mcontext;
    private LayoutInflater layoutInflator;
    private List<CardTypeInfo> list;
    private DespairTouchListener mListener;

    public CardTypeListAdapter() {
    }

    public CardTypeListAdapter(Context context, List<CardTypeInfo> items) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
    }

    public void setDespairTouchListener(DespairTouchListener listener) {
        mListener = listener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View convertView, ViewGroup arg2) {
        Component component = null;

        if (convertView == null) {
            convertView = layoutInflator.inflate(
                    R.layout.adapter_cardtype, null);
            component = new Component(convertView);
            convertView.setTag(component);
        } else
            component = (Component) convertView.getTag();

        final Component currentComponent = component;
        final int position = arg0;
        try {

            component.etName.setText(list.get(arg0).getName());
            component.btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = currentComponent.etName.getText().toString();
                    final int id = list.get(position).getId();
                    final int gid = list.get(position).getGameId();
                    CardTypeInfo despairInfo = new CardTypeInfo();
                    despairInfo.setId(id);
                    despairInfo.setGameId(gid);
                    despairInfo.setName(name);
                    mListener.onSaveBtnClickListener(despairInfo);
                }
            });

            component.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int id = list.get(position).getId();
                    final int gid = list.get(position).getGameId();
                    CardTypeInfo cardTypeInfo = new CardTypeInfo();
                    cardTypeInfo.setId(id);
                    cardTypeInfo.setGameId(gid);
                    mListener.onDelBtnClickListener(cardTypeInfo);
                }
            });

            component.etName.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //   index = position;
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public interface DespairTouchListener {
        void onSaveBtnClickListener(CardTypeInfo info);
        void onDelBtnClickListener(CardTypeInfo info);
    }

    static class Component {

        @BindView(R.id.etCardType)
        public EditText etName;

        @BindView(R.id.btnCardTypeModify)
        public Button btnSave;

        @BindView(R.id.btnCardTypeDel)
        public Button btnDel;

        public Component(View view){
            ButterKnife.bind(this, view);
        }

    }
}
