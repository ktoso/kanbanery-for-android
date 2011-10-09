package pl.project13.kanbanery.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Injector;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import pl.project13.kanbanery.util.kanbanery.UserUtils;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User>
{

   private static final String TAG = UserAdapter.class.getSimpleName();

   @Inject
   private LayoutInflater layoutInflater;

   @Inject
   private Janbanery janbanery;

   @Inject
   private FetchAndSetUserIcon fetchAndSetUserIcon;

   private SelectionActivity<User> activity;

   public UserAdapter(SelectionActivity<User> activity, Injector injector, List<User> users)
   {
      super(activity.getContext(), R.layout.user, users);
      this.activity = activity;

      injector.injectMembers(this);
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = convertView;

      if (v == null)
      {
         v = layoutInflater.inflate(R.layout.user, null);
      }

      User user = getItem(position);
      if (user != null)
      {
         populateCommentView(v, user);
      }

      return v;
   }

   private void populateCommentView(View v, final User user)
   {
      LinearLayout vRow = (LinearLayout) v.findViewById(R.id.user_row);
      ImageButton vIcon = (ImageButton) vRow.findViewById(R.id.user_icon);
      TextView vDisplayName = (TextView) vRow.findViewById(R.id.user_display_name);

      fetchAndSetUserIcon.apply(user, vIcon);

      vDisplayName.setText(UserUtils.prepareDisplayableUsername(user));

      vRow.setOnClickListener(new SelectUserOnClick(user));
   }

   private class SelectUserOnClick implements View.OnClickListener
   {
      private final User user;

      public SelectUserOnClick(User user) {this.user = user;}

      @Override
      public void onClick(View view)
      {
         activity.onSelected(user);
      }
   }
}
