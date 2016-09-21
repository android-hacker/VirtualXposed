package io.virtualapp.users;

import com.lody.virtual.os.VUserInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.lody.virtual.os.VUserManager;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import io.virtualapp.R;
import io.virtualapp.users.adapters.UserAdapter;

/**
 * @author Lody
 */

public class UserListActivity extends AppCompatActivity {

    private SwipeMenuRecyclerView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        listView = (SwipeMenuRecyclerView) findViewById(R.id.user_list);
        setup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Create user")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    createUser(editText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return true;
    }

    private void createUser(String userName) {
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Invalid user name!", Toast.LENGTH_SHORT).show();
            return;
        }
        VUserManager.get().createUser(userName, VUserInfo.FLAG_ADMIN);
        Toast.makeText(this, "Create user success!", Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void setup() {
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setItemViewSwipeEnabled(true);
        listView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(int position) {
                new AlertDialog.Builder(UserListActivity.this)
                        .setTitle("Delete user")
                        .setMessage("Are you sure to delete this user?")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            deleteUser(position);
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            refresh();
                        }).show();
            }
        });
        refresh();
    }

    private void deleteUser(int userId) {
        boolean deleted = VUserManager.get().removeUser(userId);
        Toast.makeText(this, "Remove user " + (deleted ? "success!" : "failed!"), Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void refresh() {
        listView.setAdapter(new UserAdapter(VUserManager.get().getUsers()));
    }
}
