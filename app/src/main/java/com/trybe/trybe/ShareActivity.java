package com.trybe.trybe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.trybe.trybe.app.Config;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

public class ShareActivity extends AppCompatActivity {

    Button invite;
    TextView referLink,credits;
    String refer_link;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);
        invite=(Button) findViewById(R.id.invite);
        referLink=(TextView) findViewById(R.id.referral_link_textview);
        credits=(TextView) findViewById(R.id.Credits);
        referLink.setMovementMethod(LinkMovementMethod.getInstance());
        generateLink();

        final Branch branch=Branch.getInstance();
        branch.loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                if (error==null) {
                    credits.setText(Integer.toString(branch.getCredits()));
                }
            }
        });

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                branch.userCompletedAction("Invite Button Clicked");
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_text) + "\n" + refer_link);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,"Share Using"));
            }
        });
    }

    private void generateLink() {
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                // The identifier is what Branch will use to de-dupe the content across many different Universal Objects
                .setCanonicalIdentifier("item/12345")
                // This is where you define the open graph structure and how the object will appear on Facebook or in a deepview
                .setTitle("TrYbE")
                .setContentDescription("Social Recruitment")
                .setContentImageUrl("http://www.trybe.in/Images/Website/App_share_preview.jpg")
                // You use this to specify whether this content can be discovered publicly - default is public
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                // Here is where you can add custom keys/values to the deep link data
                .addContentMetadata("picurl", "http://www.trybe.in/Images/Website/App_share_preview.jpg");



        LinkProperties linkProperties = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .addControlParameter("$desktop_url", "http://www.trybe.in") //site to open when opened in desktop
                .addControlParameter("$ios_url", "http://www.trybe.in"); //opened from ios

        branchUniversalObject.generateShortUrl(ShareActivity.this, linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    refer_link=url;
                    Log.i("MyApp", "got my Branch link to share: " + refer_link);
                    referLink.setText(refer_link);
                }
                else {
                    Log.e("error", error.toString());
                }
            }
        });
    }
}
