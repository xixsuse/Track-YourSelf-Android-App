package com.example.ihjohny.trackyourself;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.util.ArrayList;

public class Today extends Fragment{

    private DatabaseHelper databaseHelper;
    private RecyclerView todayRecyclerView;
    private TaskRecycleAdapter todayAdapter;
    private LinearLayoutManager todayLayoutManager;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private LinearLayout noTaskView;
    private TaskDetailsBottomSheet taskDetailsBottomSheet = new TaskDetailsBottomSheet();
    ArrayList<DataSet> dataList=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper=new DatabaseHelper(getContext());
        SQLiteDatabase sqLiteDatabase=databaseHelper.getWritableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_today, container, false);

        noTaskView= (LinearLayout) view.findViewById(R.id.noTaskViewId);
        todayRecyclerView= (RecyclerView) view.findViewById(R.id.taskRecyclerViewId);

        setView();
        return view;
    }

    //loadData
    private void loadData()
    {
        datePicker=new DatePicker(getContext());
        String currentDay= String.valueOf(datePicker.getDayOfMonth());
        String currentMonth= String.valueOf(datePicker.getMonth()+1);
        String currentYear= String.valueOf(datePicker.getYear());
        Cursor cursor=databaseHelper.showDataForDate(0,1,currentDay,currentMonth,currentYear);  //00 for Remaining 11 for Completed 01 for both
        dataList.clear();
        if(cursor.getCount()==0)
        {
            noTaskView.setVisibility(LinearLayout.VISIBLE);
        }
        else
        {
            noTaskView.setVisibility(LinearLayout.GONE);
            while(cursor.moveToNext())
        {
                dataList.add(new DataSet(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10)));
        }
        }
    }

    //setView
    private void setView() {
        loadData();

        todayRecyclerView.setHasFixedSize(true);
        todayLayoutManager = new LinearLayoutManager(getContext());
        todayAdapter = new TaskRecycleAdapter(getContext(), dataList, "Today");
        todayRecyclerView.setLayoutManager(todayLayoutManager);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder dragged, RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();


                DataSet data_dragged = dataList.get(position_dragged);
                DataSet data_target = dataList.get(position_target);
                databaseHelper.updateData(data_target.getId(), data_dragged.getTaskName(), data_dragged.getNote(), data_dragged.getPriority(), Integer.parseInt(data_dragged.getHours()), Integer.parseInt(data_dragged.getMinutes()), Integer.parseInt(data_dragged.getDays()), Integer.parseInt(data_dragged.getMonths()), Integer.parseInt(data_dragged.getYears()), Integer.parseInt(data_dragged.getIsCompleted()), Integer.parseInt(data_dragged.getNotif()));
                databaseHelper.updateData(data_dragged.getId(), data_target.getTaskName(), data_target.getNote(), data_target.getPriority(), Integer.parseInt(data_target.getHours()), Integer.parseInt(data_target.getMinutes()), Integer.parseInt(data_target.getDays()), Integer.parseInt(data_target.getMonths()), Integer.parseInt(data_target.getYears()), Integer.parseInt(data_target.getIsCompleted()), Integer.parseInt(data_target.getNotif()));

                ExtraFunction.setNotif(getContext(),Integer.parseInt(data_dragged.getId()),Integer.parseInt(data_target.getMinutes()),Integer.parseInt(data_target.getHours()),Integer.parseInt(data_target.getDays()),Integer.parseInt(data_target.getMonths()),Integer.parseInt(data_target.getYears()));
                ExtraFunction.setNotif(getContext(),Integer.parseInt(data_target.getId()),Integer.parseInt(data_dragged.getMinutes()),Integer.parseInt(data_dragged.getHours()),Integer.parseInt(data_dragged.getDays()),Integer.parseInt(data_dragged.getMonths()),Integer.parseInt(data_dragged.getYears()));

                loadData();
              //  todayAdapter.notifyItemMoved(position_dragged, position_target);
                todayAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                databaseHelper.makeCompleted((Integer) viewHolder.itemView.getTag());
                loadData();
                todayAdapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(todayRecyclerView);

        todayRecyclerView.setAdapter(todayAdapter);

        todayAdapter.setOnItemClickListener(new TaskRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String id = dataList.get(position).getId();
                Bundle bundle=new Bundle();
                bundle.putString("IntentTitle","Today");
                taskDetailsBottomSheet.setArguments(bundle);
                taskDetailsBottomSheet.show(getFragmentManager(), id);
                bottomSheetButton();
            }

            @Override
            public void onChecked(int position) {
                 {
                    databaseHelper.makeCompleted(Integer.parseInt(dataList.get(position).getId()));
                    loadData();
                    todayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNotifOnClick(int position) {
                databaseHelper.makeNotif(Integer.parseInt(dataList.get(position).getId()), 0);
                loadData();
                todayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNotifOffClick(int position) {
                databaseHelper.makeNotif(Integer.parseInt(dataList.get(position).getId()), 1);
                loadData();
                todayAdapter.notifyDataSetChanged();
            }
        });
    }
    public void bottomSheetButton()
    {
        taskDetailsBottomSheet.setOnButtonClickListener(new TaskDetailsBottomSheet.TaskDetailsBottomShitListener() {

            @Override
            public void onDeleteClick(int id) {
                databaseHelper.deleteData(String.valueOf(id));
                loadData();
                todayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCompletedClick(int id) {
                databaseHelper.makeCompleted(Integer.parseInt(String.valueOf(id)));
                loadData();
                todayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onUndoClick(int id) {
                databaseHelper.makeInCompleted(Integer.parseInt(String.valueOf(id)));
                loadData();
                todayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onInitClick(int id) {

            }

            @Override
            public void onEditClick(int id) {
                String title="Edit Task";
                String completed="0";
                String idd=String.valueOf(id);
                Intent intent=new Intent(getContext(),TaskDialog.class);
                intent.putExtra("IntentTitle","Home");
                intent.putExtra("Title",title);
                intent.putExtra("Id",idd);
                intent.putExtra("Completed",completed);
                startActivityForResult(intent,5);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==5)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                loadData();
                todayAdapter.notifyDataSetChanged();
            }
        }
    }

}
