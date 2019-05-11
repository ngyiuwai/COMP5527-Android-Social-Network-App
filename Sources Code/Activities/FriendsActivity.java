package comp5527.android.social_network_v03;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef =  FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        //UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        updateUserStatus("offline");
    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model)
            {
                holder.friendsDate.setText("Friends Since: " + model.getDate());

                final String usersIDs = getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState"))
                            {
                                type = dataSnapshot.child("userState").child("type").getValue().toString();

                                if(type.equals("online"))
                                {
                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.setFullname(userName);
                            holder.setProfileimage(profileImage);

                            holder.mView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick (View v)
                                {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                              userName + "'s Profile",
                                              "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if (which == 0)
                                            {
                                                Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileintent.putExtra("visit_user_id", usersIDs);
                                                startActivity(profileintent);
                                            }
                                            if (which == 1)
                                            {
                                                Intent Chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                Chatintent.putExtra("visit_user_id", usersIDs);
                                                Chatintent.putExtra("userName", userName);
                                                startActivity(Chatintent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        myFriendList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView myImage;
        TextView myName, friendsDate;

        View mView;
        ImageView onlineStatusView;

        public FriendsViewHolder (View itemView)
        {
            super (itemView);

            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);

            friendsDate = itemView.findViewById(R.id.all_users_status);
        }

        public void setProfileimage(String profileimage)
        {
            myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).into(myImage);
        }

        public void setFullname(String fullname)
        {
            myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
    }
}
