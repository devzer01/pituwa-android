package org.gnuzero.pub.pituwa.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.pkmmte.view.CircularImageView;

import java.util.List;

import org.gnuzero.pub.pituwa.ProfileActivity;
import org.gnuzero.pub.pituwa.R;
import org.gnuzero.pub.pituwa.app.App;
import org.gnuzero.pub.pituwa.constants.Constants;
import org.gnuzero.pub.pituwa.model.Comment;
import org.gnuzero.pub.pituwa.util.CommentInterface;
import org.gnuzero.pub.pituwa.util.TagSelectingTextview;
import org.gnuzero.pub.pituwa.view.ResizableImageView;


public class CommentListAdapter extends BaseAdapter implements Constants {

	private Activity activity;
	private LayoutInflater inflater;
	private List<Comment> commentsList;

    private CommentInterface responder;

    private Boolean myPost = false;

    TagSelectingTextview mTagSelectingTextview;

    public static int hashTagHyperLinkEnabled = 1;
    public static int hashTagHyperLinkDisabled = 0;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

	public CommentListAdapter(Activity activity, List<Comment> commentsList, CommentInterface responder) {

		this.activity = activity;
		this.commentsList = commentsList;
        this.responder = responder;

        mTagSelectingTextview = new TagSelectingTextview();
	}

    public void setMyPost(Boolean myPost) {

        this.myPost = myPost;
    }

    public Boolean getMyPost() {

        return this.myPost;
    }

	@Override
	public int getCount() {

		return commentsList.size();
	}

	@Override
	public Object getItem(int location) {

		return commentsList.get(location);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	static class ViewHolder {

        public CircularImageView commentAuthorPhoto;
        public TextView commentAuthor;
        public TextView commentText;
        public TextView commentTimeAgo;
        public TextView commentLikesCount;
        public ImageView commentLike;
        public ImageView commentAction;
        public ResizableImageView commentImg;
	        
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;

		if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

		if (convertView == null) {
			
			convertView = inflater.inflate(R.layout.comment_list_row, null);
			
			viewHolder = new ViewHolder();

            viewHolder.commentAuthorPhoto = (CircularImageView) convertView.findViewById(R.id.commentAuthorPhoto);
            viewHolder.commentImg = (ResizableImageView) convertView.findViewById(R.id.commentImg);
            viewHolder.commentLike = (ImageView) convertView.findViewById(R.id.commentLike);
            viewHolder.commentAction = (ImageView) convertView.findViewById(R.id.commentAction);
            viewHolder.commentLikesCount = (TextView) convertView.findViewById(R.id.commentLikesCount);
			viewHolder.commentText = (TextView) convertView.findViewById(R.id.commentText);
            viewHolder.commentText.setTypeface(App.getInstance().getFont());
            viewHolder.commentAuthor = (TextView) convertView.findViewById(R.id.commentAuthor);
            viewHolder.commentTimeAgo = (TextView) convertView.findViewById(R.id.commentTimeAgo);

//            viewHolder.questionRemove.setTag(position);
            convertView.setTag(viewHolder);

		} else {
			
			viewHolder = (ViewHolder) convertView.getTag();
		}

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }

        viewHolder.commentAuthorPhoto.setTag(position);
        viewHolder.commentText.setTag(position);
        viewHolder.commentAuthor.setTag(position);
        viewHolder.commentTimeAgo.setTag(position);
        viewHolder.commentImg.setTag(position);
        viewHolder.commentAction.setTag(position);
        viewHolder.commentLikesCount.setTag(position);
        viewHolder.commentLike.setTag(position);
		
		final Comment comment = commentsList.get(position);

        viewHolder.commentAuthor.setVisibility(View.VISIBLE);
        viewHolder.commentAuthor.setText(comment.getFromUserFullname());

        viewHolder.commentAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("profileId", comment.getFromUserId());
                activity.startActivity(intent);
            }
        });

        if (comment.getFromUserPhotoUrl() != null && comment.getFromUserPhotoUrl().length() != 0) {

            viewHolder.commentAuthorPhoto.setVisibility(View.VISIBLE);

            imageLoader.get(comment.getFromUserPhotoUrl(), ImageLoader.getImageListener(viewHolder.commentAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

        } else {

            viewHolder.commentAuthorPhoto.setVisibility(View.VISIBLE);
            viewHolder.commentAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
        }

        viewHolder.commentAuthorPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("profileId", comment.getFromUserId());
                activity.startActivity(intent);
            }
        });

        if (App.getInstance().getId() != 0) {

            viewHolder.commentAction.setVisibility(View.VISIBLE);

        } else {

            viewHolder.commentAction.setVisibility(View.GONE);
        }

        viewHolder.commentAction.setImageResource(R.drawable.ic_action_collapse);

        viewHolder.commentAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int getPosition = (Integer) view.getTag();

                responder.commentAction(getPosition);
            }
        });

        String commentText = (String) comment.getText();
        if (commentText != null) commentText = commentText.replaceAll("<br>", "\n");
        viewHolder.commentText.setText(commentText);

        viewHolder.commentText.setMovementMethod(LinkMovementMethod.getInstance());

        String textHtml = comment.getText();

        viewHolder.commentText.setText(textHtml);

        viewHolder.commentText.setVisibility(View.VISIBLE);

        String timeAgo;

        timeAgo = comment.getTimeAgo();

        if (comment.getReplyToUserId() != 0) {

            if (comment.getReplyToUserFullname().length() != 0) {

                timeAgo = timeAgo + " " + activity.getString(R.string.label_to) + " " + comment.getReplyToUserFullname();

            } else {

                timeAgo = timeAgo + " " + activity.getString(R.string.label_to) + " @" + comment.getReplyToUserUsername();
            }
        }

        viewHolder.commentTimeAgo.setVisibility(View.VISIBLE);
        viewHolder.commentTimeAgo.setText(timeAgo);

        viewHolder.commentImg.setVisibility(View.GONE);

        viewHolder.commentLike.setVisibility(View.GONE);
        viewHolder.commentLikesCount.setVisibility(View.GONE);
        viewHolder.commentLike.setImageResource(R.drawable.perk);

		return convertView;
	}
}