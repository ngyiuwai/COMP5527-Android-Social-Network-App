package comp5527.android.social_network_v03;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String searchBoxInput = SearchInputText.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput)
    {
        Toast.makeText(this, "Searching....", Toast.LENGTH_LONG).show();

        Query searchPeopleAndFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options=new FirebaseRecyclerOptions.Builder<FindFriends>().
                setQuery(searchPeopleAndFriendsQuery, FindFriends.class).build(); //query build past the query to FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsActivity.FindFriendViewHolder holder, final int position, @NonNull FindFriends model)
            {
                final String PostKey = getRef(position).getKey();
                holder.username.setText(model.getFullname());
                holder.status.setText(model.getStatus());

                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent findOthersIntent = new Intent(FindFriendsActivity.this, FindFriendsActivity.class);
                        findOthersIntent.putExtra("PostKey", PostKey);
                        startActivity(findOthersIntent);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
            @NonNull
            @Override
            public FindFriendsActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);

                FindFriendsActivity.FindFriendViewHolder viewHolder=new FindFriendsActivity.FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, status;
        CircleImageView profileimage;
        View mView;

        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.all_users_profile_full_name);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
