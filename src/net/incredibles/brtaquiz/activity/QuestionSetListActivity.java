package net.incredibles.brtaquiz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import net.incredibles.brtaquiz.R;
import net.incredibles.brtaquiz.controller.QuestionSetListController;
import roboguice.activity.RoboListActivity;

import java.util.List;

import static net.incredibles.brtaquiz.controller.QuestionSetListController.QuestionSet;

/**
 * @author sharafat
 * @Created 2/16/12 6:09 PM
 */
public class QuestionSetListActivity extends RoboListActivity {
    @Inject
    private QuestionSetListController questionSetListController;

    private List<QuestionSet> questionSets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_set_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO: use AsyncTask for the followings
        questionSets = questionSetListController.getQuestionSets();
        setListAdapter(new QuestionSetListAdapter(this, questionSets));
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        questionSetListController.selectQuestionSet(questionSets.get(position));
        startActivity(new Intent(this, QuestionActivity.class));
        finish();
    }


    private class QuestionSetListAdapter extends ArrayAdapter<QuestionSet> {
        private Context context;
        private List<QuestionSet> questionSets;

        public QuestionSetListAdapter(Context context, List<QuestionSet> questionSets) {
            super(context, 0, questionSets);
            this.context = context;
            this.questionSets = questionSets;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.question_set_list_entry, null);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.complete_icon);
            TextView questionSetNameTextView = (TextView) convertView.findViewById(R.id.question_set_name_text_view);
            TextView questionSetDetailsTextView = (TextView) convertView.findViewById(R.id.question_set_details);

            QuestionSet questionSet = questionSets.get(position);
            questionSetNameTextView.setText(questionSet.getSignSet().getName());
            questionSetDetailsTextView.setText(getQuestionSetDetailsText(questionSet));
            if (questionSet.isComplete()) {
                imageView.setImageResource(R.drawable.ic_test_complete);
            }

            return convertView;
        }

        private String getQuestionSetDetailsText(QuestionSet questionSet) {
            return context.getString(R.string.questions) + " " + questionSet.getTotalQuestions() + "   "
                    + context.getString(R.string.answered) + " " + questionSet.getAnswered();
        }
    }

}
