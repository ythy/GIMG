package com.mx.gillustrated.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import com.mx.gillustrated.R;
import com.mx.gillustrated.vo.CharacterInfo;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CharacterListAdapter  extends BaseAdapter {

    private Context mcontext;
    private LayoutInflater layoutInflator;
    private List<CharacterInfo> list;
    private CharacterListAdapter.CharacterTouchListener mListener;

    public CharacterListAdapter() {
    }

    public CharacterListAdapter(Context context, List<CharacterInfo> items) {
        mcontext = context;
        layoutInflator = LayoutInflater.from(mcontext);
        list = items;
    }

    public void setCharacterTouchListener(CharacterListAdapter.CharacterTouchListener listener) {
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
        CharacterListAdapter.Component component = null;

        if (convertView == null) {
            convertView = layoutInflator.inflate(
                    R.layout.adapter_character, null);
            component = new CharacterListAdapter.Component(convertView);
            convertView.setTag(component);
        } else
            component = (CharacterListAdapter.Component) convertView.getTag();

        final CharacterListAdapter.Component currentComponent = component;
        final int position = arg0;
        try {

            component.etName.setText(list.get(arg0).getName());
            component.spinnerNational.setSelection(list.get(arg0).getNationality());
            component.spinnerDomain.setSelection(list.get(arg0).getDomain());
            component.spinnerAge.setSelection(list.get(arg0).getAge());
            component.spinnerSkilled.setSelection(list.get(arg0).getSkilled());
            component.spinnerChar.setSelection(list.get(arg0).getCharacter());
            final Component finalComponent = component;
            component.btnModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = currentComponent.etName.getText().toString();
                    final int id = list.get(position).getId();
                    final int gid = list.get(position).getGameId();
                    CharacterInfo characterInfo = new CharacterInfo();
                    characterInfo.setGameId(gid);
                    characterInfo.setId(id);
                    characterInfo.setName(name);
                    characterInfo.setNationality(finalComponent.spinnerNational.getSelectedItemPosition());
                    characterInfo.setDomain(finalComponent.spinnerDomain.getSelectedItemPosition());
                    characterInfo.setAge(finalComponent.spinnerAge.getSelectedItemPosition());
                    characterInfo.setSkilled(finalComponent.spinnerSkilled.getSelectedItemPosition());
                    characterInfo.setCharacter(finalComponent.spinnerChar.getSelectedItemPosition());
                    mListener.onSaveBtnClickListener(characterInfo, position);
                }
            });

            component.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int id = list.get(position).getId();
                    final int gid = list.get(position).getGameId();
                    CharacterInfo cardTypeInfo = new CharacterInfo();
                    cardTypeInfo.setId(id);
                    cardTypeInfo.setGameId(gid);
                    mListener.onDelBtnClickListener(cardTypeInfo, position);
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

    public interface CharacterTouchListener {
        void onSaveBtnClickListener(CharacterInfo info, int index);
        void onDelBtnClickListener(CharacterInfo info, int index);
    }

    static class Component {

        @BindView(R.id.etCharName)
        public EditText etName;


        @BindView(R.id.spinnerChar)
        public Spinner spinnerChar;

        @BindView(R.id.spinnerNational)
        public Spinner spinnerNational;

        @BindView(R.id.spinnerDomain)
        public Spinner spinnerDomain;

        @BindView(R.id.spinnerAge)
        public Spinner spinnerAge;

        @BindView(R.id.spinnerSkilled)
        public Spinner spinnerSkilled;

        @BindView(R.id.btnCharModify)
        public ImageButton btnModify;

        @BindView(R.id.btnCharDel)
        public ImageButton btnDel;

        public Component(View view){
            ButterKnife.bind(this, view);
        }

    }
}
