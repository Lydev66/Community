package com.lyx.mycommunity.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lyx.mycommunity.adapter.ListAdapter;
import com.lyx.mycommunity.bean.NoteInfo;
import com.lyx.mycommunity.dao.Note;
import com.lyx.mycommunity.dao.NoteDataBaseHelper;
import com.lyx.shoppingcity.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.lyx.mycommunity.dao.Note.deleteAllNote;


public class NoteShowActivity extends AppCompatActivity {
private Toolbar toolbar;
    EditText mEditSearch;//搜索框
    private ImageButton mTvSearch;//搜索按钮
    private Button add;//添加按钮
    private Button delete;//删除按钮
    private Button up,btn_back;//升序
    private Button down;//降序
    private ListView noteListView;
    private List<NoteInfo> noteList = new ArrayList<>();
    private List<NoteInfo> noteListup = new ArrayList<>();
    private List<NoteInfo> noteListdown = new ArrayList<>();
    private List<NoteInfo> notesearchList = new ArrayList<>();
    private ListAdapter mListAdapter;
    private ListAdapter mListSearchAdapter;
    private static NoteDataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_show);
        dbHelper = new NoteDataBaseHelper(this,"MyNote.db",null,1);
        initView();
        initListener();
    }

    private void initView(){
        add = (Button) findViewById(R.id.btn_add);
        delete = (Button) findViewById(R.id.btn_delete);
        toolbar = findViewById(R.id.toolbar);
        noteListView = (ListView) findViewById(R.id.note_list);
        add = (Button) findViewById(R.id.btn_add);
        mTvSearch = (ImageButton) findViewById(R.id.btn_search);
        mEditSearch = (EditText) findViewById(R.id.edit_search);
        up = (Button) findViewById(R.id.btn_up);
        down = (Button) findViewById(R.id.btn_down);
        //获取noteList
        getNoteList();
        mListAdapter = new ListAdapter(getApplicationContext(),noteList);
        noteListView.setAdapter(mListAdapter);
    }


    //搜索刷新列表
    private void refreshListView() {
        mListAdapter.notifyDataSetChanged();
        noteListView.setAdapter(mListAdapter);
    }

    private void initListener() {
        //跳转回主界面 刷新列表
        Intent intent1 = getIntent();
        if (intent1 != null){
            getNoteList();
            mListAdapter.refreshDataSet();//渲染列表
        }
        toolbar.setNavigationIcon(R.mipmap.top_bar_left_back1);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllNote(dbHelper);
                getNoteList();
                refreshListView();
                mListAdapter.refreshDataSet();
                Toast.makeText(NoteShowActivity.this,"删除成功！",Toast.LENGTH_LONG).show();

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteShowActivity.this,NoteEditorActivity.class);
                startActivity(intent);
                NoteShowActivity.this.finish();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryDataUp();
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryDataDown();
            }
        });
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteInfo noteInfo = noteList.get(position);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("noteInfo",(Serializable)noteInfo);
                intent.putExtras(bundle);
                intent.setClass(NoteShowActivity.this, NoteEditorActivity.class);
                startActivity(intent);
                NoteShowActivity.this.finish();
            }
        });
        mTvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //隐藏键盘
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        /**
         * EditText搜索框对输入值变化的监听，实时搜索
         */
        // 3、使用TextWatcher实现对实时搜索
         mEditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String searchString = mEditSearch.getText().toString();
                queryData(searchString);
            }
        });

        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final NoteInfo noteInfo = noteList.get(position);
                String title = "提示";
                new AlertDialog.Builder(NoteShowActivity.this)
                        .setIcon(R.drawable.book)
                        .setTitle(title)
                        .setMessage("确定要删除吗?")
                        .setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Note.deleteNote(dbHelper,Integer.parseInt(noteInfo.getId()));
                                refreshListView();
                                noteList.remove(position);
                                mListAdapter.refreshDataSet();
                                Toast.makeText(NoteShowActivity.this,"删除成功！",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                return true;
            }
        });
    }





    //从数据库中读取所有帖子 封装成List<NoteInfo>
    private void getNoteList(){
        noteList.clear();
        Cursor allNotes = Note.getAllNotes(dbHelper);
        noteInfoSet(noteList,allNotes);
    }


    private void noteInfoSet(List<NoteInfo> noteinfo,Cursor Notes){
        for (Notes.moveToFirst(); !Notes.isAfterLast(); Notes.moveToNext()){
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setId(Notes.getString(Notes.getColumnIndex(Note._id)));
            noteInfo.setTitle(Notes.getString(Notes.getColumnIndex(Note.title)));
            noteInfo.setContent(Notes.getString(Notes.getColumnIndex(Note.content)));
            noteInfo.setDate(Notes.getString(Notes.getColumnIndex(Note.time)));
            noteInfo.setDes(Notes.getString(Notes.getColumnIndex(Note.content)));
            noteInfo.setPre(Notes.getString(Notes.getColumnIndex(Note.pre)));
            noteInfo.setPhoto(Notes.getBlob(Notes.getColumnIndex(Note.picture)));
            noteinfo.add(noteInfo);
        }
    }


    private void getSearchList(String searchData){
        notesearchList.clear();
        Cursor allNotes = Note.getSearchNotes(dbHelper,searchData);
        noteInfoSet(notesearchList,allNotes);
    }


    private void getListup(){
        noteListup.clear();
        Cursor upNotes = Note.upNotes(dbHelper);
        noteInfoSet(noteListup,upNotes);
    }


    private void getListDown(){
        noteListdown.clear();
        Cursor downNotes = Note.downNotes(dbHelper);
        noteInfoSet(noteListdown,downNotes);
    }

    /**
     * 搜索数据库中的数据
     *
     * @param searchData
     */
    private void queryData(String searchData) {

        getSearchList(searchData);
        mListSearchAdapter = new ListAdapter(NoteShowActivity.this,notesearchList);
        noteListView.setAdapter( mListSearchAdapter);
        mListSearchAdapter.refreshDataSet();//渲染列表
    }

    private void queryDataUp() {
        getListup();
        mListSearchAdapter = new ListAdapter(NoteShowActivity.this,noteListup);
        noteListView.setAdapter( mListSearchAdapter);
        mListSearchAdapter.refreshDataSet();//渲染列表
    }

    private void queryDataDown() {
        getListDown();
        mListSearchAdapter = new ListAdapter(NoteShowActivity.this,noteListdown);
        noteListView.setAdapter( mListSearchAdapter);
        mListSearchAdapter.refreshDataSet();//渲染列表
    }

    public static NoteDataBaseHelper getDbHelper() {
        return dbHelper;
    }


}
