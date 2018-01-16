package layout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bodhileaf.agriMonitor.LoginActivity;
import com.bodhileaf.agriMonitor.MyDatabase;
import com.bodhileaf.agriMonitor.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link config_node_association.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link config_node_association#newInstance} factory method to
 * create an instance of this fragment.
 */
public class config_node_association extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Config node association";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Integer nodeId;
    private Integer nodeType;
    private String dbFileName;
    private List<String> nodeList;
    private SQLiteDatabase agriDb;
    private ArrayAdapter<String> nodeAdapter;
    private Spinner nodeSpinner;
    private Spinner sensorSpinner;
    private Integer listCurPos;
    private boolean match = false;
    private int nodeCnt=0;
    private int deleteNodePosition;
    private OnFragmentInteractionListener mListener;
    private DriveFolder curFolder;

    List<String> sensor_list;
    List<String> all_sensor_list;
    private DriveFile farmDriveFile;
    ArrayAdapter<String> sensorAdapter;
    ArrayAdapter<String> allSensorAdapter;
    HashMap<Integer, Integer> sensorTypeHashList;
    private  DriveResourceClient mDriveClient;
    private Task<Void> getDatabaseUpdateTask;


    public config_node_association() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment config_node_association.
     */
    // TODO: Rename and change types and number of parameters
    public static config_node_association newInstance(String param1, String param2) {
        config_node_association fragment = new config_node_association();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras == null) {
            Log.d("schedule frag", "onCreate: null bundle ");
            return;
        }
        nodeId = extras.getInt("nodeId");
        nodeType = extras.getInt("nodeType");
        dbFileName = extras.getString("configfilename");
        Log.d("schedule frag Node info", "onCreate: nodeid/nodetype "+ String.valueOf(nodeId)+"/"+String.valueOf(nodeType));
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config_node_association, container, false);

        sensor_list = new ArrayList<String>();
        mDriveClient=  Drive.getDriveResourceClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()));
        getDatabaseUpdateTask =
                mDriveClient.getRootFolder()
                        .continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
                            @Override
                            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                                Query query = new Query.Builder()
                                        .addFilter(Filters.eq(SearchableField.TITLE, "bodhKrishiDatabases"))
                                        .build();
                                DriveFolder rootFolder = task.getResult();
                                return mDriveClient.queryChildren(rootFolder,query);
                            }
                        })
                        .continueWithTask(new Continuation<MetadataBuffer, Task<MetadataBuffer>>() {
                            @Override
                            public Task<MetadataBuffer> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                                String farmName = dbFileName.substring(dbFileName.lastIndexOf("/")+1,dbFileName.lastIndexOf("."))+".db";
                                Query query = new Query.Builder()
                                        .addFilter(Filters.eq(SearchableField.TITLE, farmName))
                                        .build();
                                DriveFolder rootFolder = task.getResult().get(0).getDriveId().asDriveFolder();;
                                return mDriveClient.queryChildren(rootFolder,query);
                            }
                        })
                        .continueWithTask(new Continuation<MetadataBuffer, Task<DriveContents>>() {
                            @Override
                            public Task<DriveContents> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                                farmDriveFile = task.getResult().get(0).getDriveId().asDriveFile();
                                return mDriveClient.openFile(farmDriveFile, DriveFile.MODE_READ_WRITE);
                            }
                        })
                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                DriveContents driveContents = task.getResult();
                                ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
                                File iFile = new File(dbFileName);

                                try {
                                    InputStream in = new FileInputStream(iFile);
                                    OutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                                    MyDatabase.writeExtractedFileToDisk(in,out);
                                }catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }


                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setStarred(true)
                                        .setLastViewedByMeDate(new Date())
                                        .build();
                                Task<Void> commitTask =
                                        mDriveClient.commitContents(driveContents, changeSet);

                                return commitTask;
                            }
                        })
                        .addOnSuccessListener(getActivity(),
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void avoid) {

                                        Log.d(TAG, "farm config updated into drive");
                                    }
                                }
                        )
                        .addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Unable to create folder", e);
                            }
                        });
        sensorTypeHashList = new HashMap<Integer, Integer>();
        agriDb = getActivity().openOrCreateDatabase(dbFileName, android.content.Context.MODE_PRIVATE, null);
        initNodeList(agriDb);
        sensorAdapter = new ArrayAdapter<String>(getActivity(),R.layout.row_item,R.id.node_name,sensor_list);
        ListView listView = (ListView) view.findViewById(R.id.nodeListView);
        listView.setAdapter(sensorAdapter);
        // Create a message handling object as an anonymous class.
        listView.setOnItemClickListener(mMessageClickedHandler);

        //setup Node Id Spinner
        nodeSpinner =  view.findViewById(R.id.sensor_nodeid_spinner_node_asc);
        nodeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, nodeList);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        nodeSpinner.setSelection(listCurPos);
        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String listEntry = nodeList.get(position).toString();
                sensor_list.clear();
                sensorTypeHashList.clear();
                    //case where the selected node already existed
                    String query = String.format("SELECT * FROM nodeAssociations where actuatorNodeID is %s" ,listEntry);
                    Log.d(TAG, "onItemSelected: query: "+query);
                    //check if the schedule id exists for the node id
                    Cursor nodeListResults = agriDb.rawQuery(query,null);
                    nodeListResults.moveToFirst();
                    if (nodeListResults.getCount() == 0) {
                        nodeCnt=0;
                        //Context curContext=view.getContext();
                        //if(curContext == null) {
                          Context  curContext = getActivity();
                        //}
                        if(curContext != null) {
                            Toast.makeText(curContext, "No associated nodes", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //select the node count from the result
                        nodeCnt = nodeListResults.getInt(1);
                        Log.d(TAG, "onItemSelected: nodecnt: "+String.format("%d",nodeCnt));
                        for (int i=2; i<= 1+nodeCnt;i++) {
                            Integer val =  nodeListResults.getInt(i);
                            query = "SELECT nodeType FROM nodesInfo WHERE nodeID="+val.toString();
                            Cursor nodeResult=agriDb.rawQuery(query,null);
                            nodeResult.moveToFirst();
                            Log.d(TAG, "onItemSelected: sensornode: "+String.format("%d",val));
                            sensor_list.add(val.toString());
                            sensorTypeHashList.put(val,nodeResult.getInt(0));
                            nodeResult.close();
                        }

                    }
                sensorAdapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

         //setup sensor list
         sensorSpinner = view.findViewById(R.id.nodeid_spinner_node_asc);
         allSensorAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, all_sensor_list);
         allSensorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         sensorSpinner.setAdapter(allSensorAdapter);
         sensorSpinner.setSelection(0);
         sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button add_sensor_button = view.findViewById(R.id.button_add_node_sensor_asc);
        add_sensor_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query;
                sensor_list.add(sensorSpinner.getSelectedItem().toString());
                //put sensor_type in hash
                query = "SELECT nodeType FROM nodesInfo WHERE nodeID="+nodeSpinner.getSelectedItem().toString();
                Cursor nodeResult=agriDb.rawQuery(query,null);
                nodeResult.moveToFirst();
                sensorTypeHashList.put(Integer.valueOf(nodeSpinner.getSelectedItem().toString()),nodeResult.getInt(0));
                nodeResult.close();
                nodeCnt++;
                if(nodeCnt == 1) {
                    query = String.format("INSERT into nodeAssociations(actuatorNodeID,associatedNodesCount,nodeID1) VALUES(%s,1,%s)",nodeSpinner.getSelectedItem().toString(),sensorSpinner.getSelectedItem().toString());
                    //new entry needs to be created in the table as its the first node to be added
                } else {
                    query = String.format("UPDATE nodeAssociations set associatedNodesCount=%d, nodeID%d=%s WHERE actuatorNodeID=%s",
                            nodeCnt, nodeCnt, sensorSpinner.getSelectedItem().toString(), nodeSpinner.getSelectedItem().toString());
                }
                Log.d(TAG, "add sensor onClick query: "+query);
                agriDb.execSQL(query);
                sensorAdapter.notifyDataSetChanged();
                getDatabaseUpdateTask.getResult();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public  void onItemClick(AdapterView parent, View v, int position, long id) {
            deleteNodePosition = position;
            // provide option to delete node in alert box
// 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
            builder.setPositiveButton("Delete Node?", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id) {
                    String query;
                    // User clicked OK button
                    //sql query to remove the node from association
                    nodeCnt--;
                    if(nodeCnt == 0) {
                        query = String.format("DELETE FROM nodeAssociations where actuatorNodeID=%s", nodeSpinner.getSelectedItem().toString());
                    } else {
                        query = String.format("UPDATE nodeAssociations set associatedNodesCount=%d, nodeID%d=%s WHERE actuatorNodeID=%s",
                                nodeCnt,deleteNodePosition+1,sensor_list.get(deleteNodePosition),nodeSpinner.getSelectedItem().toString());
                    }
                    Log.d(TAG, "onClick: delete node query :"+query);
                    agriDb.execSQL(query);
                    sensor_list.remove(deleteNodePosition);
                    sensorTypeHashList.remove(deleteNodePosition);
                    sensorAdapter.notifyDataSetChanged();
                    getDatabaseUpdateTask.getResult();

                   // Task<DriveContents> openTask =
                     //       mDriveClient.openFile(file, DriveFile.MODE_READ_WRITE);

                }
            });
            //builder.setNegativeButton("Navigate to farm", new DialogInterface.OnClickListener() {
            //    @Override
            //    public void onClick(DialogInterface dialog, int which) {
            //        //got to maps activity based on location
            //    }
            //});

// 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();

            //Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            //nbutton.setBackgroundResource(R.drawable.sensor);
            //Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            //pbutton.setBackgroundResource(android.R.drawable.ic_menu_delete);

            //Set negative button background
        }
    };
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private void initNodeList(SQLiteDatabase agriDb) {

        Integer curPos = 0;
        nodeList = new ArrayList<String>();
        all_sensor_list = new ArrayList<String>();
        String query = "SELECT nodeID FROM nodesInfo where nodeType is 2";
        //check if the schedule id exists for the node id
        Cursor nodeListResults = agriDb.rawQuery(query,null);
        nodeListResults.moveToFirst();
        if(nodeListResults.getCount() >0 ) {
            do {

                Integer nodeID_from_list = nodeListResults.getInt(0);
                if (nodeId == nodeID_from_list) {
                    match = true;
                    listCurPos = curPos;
                }
                nodeList.add(nodeID_from_list.toString());
                Log.d("node association", "NodeList: " + nodeID_from_list.toString());
                curPos++;
            } while (nodeListResults.moveToNext());
        }
        nodeListResults.close();
        if(match == false) {
            listCurPos = curPos;
            nodeList.add(nodeId.toString());
        }
        query = "SELECT nodeID FROM nodesInfo where nodeType in (0,1)";
        //check if the schedule id exists for the node id
        Cursor sensorListResults = agriDb.rawQuery(query,null);
        sensorListResults.moveToFirst();
        if(sensorListResults.getCount()>0) {
            do {
                Integer nodeID_from_list = sensorListResults.getInt(0);
                all_sensor_list.add(nodeID_from_list.toString());
            } while (sensorListResults.moveToNext());
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
