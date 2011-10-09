package pl.project13.kanbanery.ui.adapters;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.inject.Inject;
import com.google.inject.Injector;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Comment;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.util.SimpleAsyncTask;
import pl.project13.kanbanery.util.cache.Cache;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;

import java.util.List;

class CommentAdapter extends ArrayAdapter<Comment>
{

   private static final String TAG = CommentAdapter.class.getSimpleName();

   @Inject
   private LayoutInflater layoutInflater;

   @Inject
   private Janbanery janbanery;

   @Inject
   private Cache<User> userCache;

   @Inject
   private FetchAndSetUserIcon fetchAndSetUserIcon;

   public CommentAdapter(Context context, Injector injector, List<Comment> comments) {
      super(context, R.layout.comment, comments);

      injector.injectMembers(this);

      this.fetchAndSetUserIcon = fetchAndSetUserIcon;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = convertView;

      if (v == null)
      {
         v = layoutInflater.inflate(R.layout.comment, null);
      }

      Comment comment = getItem(position);
      if (comment != null)
      {
         populateCommentView(v, comment);
      }

      return v;
   }

   private void populateCommentView(View v, Comment comment)
   {
      TextView commentBody = (TextView) v.findViewById(R.id.body);
      ImageButton commenterImageBtn = (ImageButton) v.findViewById(R.id.commenter_image_btn);

      commentBody.setText(comment.getBody());
      Linkify.addLinks(commentBody, Linkify.ALL);

      setUserIcon(commenterImageBtn, comment.getAuthorId());
   }

   private void setUserIcon(final ImageView taskIcon, final Long ownerId)
   {
      String ownerKey = String.valueOf(ownerId);

      if (userCache.isCacheHit(ownerKey))
      {
         User user = userCache.get(ownerKey);

         fetchAndSetUserIcon.apply(user, taskIcon);
      }
      else
      {
         User user = new SimpleAsyncTask<User>()
         {
            @Override
            public User doInBackground()
            {
               User user = janbanery.users().byId(ownerId);
               return user;
            }
         }.get();

         userCache.cache(ownerKey, user);

         fetchAndSetUserIcon.apply(user, taskIcon);
      }
   }
}
