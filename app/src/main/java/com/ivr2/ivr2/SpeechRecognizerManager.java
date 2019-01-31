package com.ivr2.ivr2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


public class SpeechRecognizerManager {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    /* Keyword we are looking for to activate menu */
    public static final String OK_AMEER = "HEY JARVIS";
    private edu.cmu.pocketsphinx.SpeechRecognizer mPocketSphinxRecognizer;
    private static final String TAG = SpeechRecognizerManager.class.getSimpleName();
    private Context mContext;
    private OnMagicWordListener onmagicWordListener;


    public SpeechRecognizerManager(Context context) {
        this.mContext = context;
        initPockerSphinx();


    }


    private void initPockerSphinx() {

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(mContext);

                    //Performs the synchronization of assets in the application and external storage
                    File assetDir = assets.syncAssets();

                    //Creates a new SpeechRecognizer builder with a default configuration
                    SpeechRecognizerSetup speechRecognizerSetup = defaultSetup();

                    //Set Dictionary and Acoustic Model files
                    speechRecognizerSetup.setAcousticModel(new File(assetDir, "en-us-ptm"));
                    speechRecognizerSetup.setDictionary(new File(assetDir, "voice.dict"));

                    // Threshold to tune for keyphrase to balance between false positives and false negatives
                    speechRecognizerSetup.setKeywordThreshold(1e-45f);

                    //Creates a new SpeechRecognizer object based on previous set up.
                    mPocketSphinxRecognizer = speechRecognizerSetup.getRecognizer();

                    mPocketSphinxRecognizer.addListener(new PocketSphinxRecognitionListener());

                    // Create keyword-activation search.
                    mPocketSphinxRecognizer.addKeyphraseSearch(KWS_SEARCH, OK_AMEER);
                    mPocketSphinxRecognizer.addKeyphraseSearch(KWS_SEARCH, OK_AMEER);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(mContext, "Failed to init mPocketSphinxRecognizer ", Toast.LENGTH_SHORT).show();
                } else {
                    restartSearch(KWS_SEARCH);
                }
            }
        }.execute();

    }




    public void destroy() {
        if (mPocketSphinxRecognizer != null) {
            mPocketSphinxRecognizer.cancel();
            mPocketSphinxRecognizer.shutdown();
            mPocketSphinxRecognizer = null;
        }
    }

    public void cancel(){
        if (mPocketSphinxRecognizer!=null)
            mPocketSphinxRecognizer.cancel();

    }

    public void restartSearch(String searchName) {

        mPocketSphinxRecognizer.stop();

        mPocketSphinxRecognizer.startListening(searchName);

    }
    public void startListening(String word){
        if (mPocketSphinxRecognizer!=null)
            mPocketSphinxRecognizer.startListening(OK_AMEER);
    }


    protected class PocketSphinxRecognitionListener implements edu.cmu.pocketsphinx.RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
        }


        /**
         * In partial result we get quick updates about current hypothesis. In
         * keyword spotting mode we can react here, in other modes we need to wait
         * for final result in onResult.
         */
        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
            {
                Log.d(TAG,"null");


                return;

            }


            String text = hypothesis.getHypstr();

            if (text.equalsIgnoreCase(OK_AMEER)) {
                onmagicWordListener.OnMagicWordDeceted(text);
                Toast.makeText(mContext, "You said: "+text, Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onResult(Hypothesis hypothesis) {
        }


        /**
         * We stop mPocketSphinxRecognizer here to get a final result
         */
        @Override
        public void onEndOfSpeech() {

        }

        public void onError(Exception error) {
        }

        @Override
        public void onTimeout() {
        }

    }




    public void setOnResultListner(OnMagicWordListener onmagicWordListener){
        this.onmagicWordListener=onmagicWordListener;
    }

    public interface OnMagicWordListener
    {
        void OnMagicWordDeceted(String word);
    }
}