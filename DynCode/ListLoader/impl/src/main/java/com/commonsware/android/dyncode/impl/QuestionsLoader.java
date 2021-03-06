/***
 Copyright (c) 2016 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.commonsware.android.dyncode.impl;

import android.util.Log;
import com.commonsware.android.dyncode.api.Thing;
import com.commonsware.android.dyncode.api.ThingsLoadedEvent;
import com.commonsware.android.dyncode.api.ThingsLoader;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionsLoader implements ThingsLoader {
  private SOQuestions rawResult;
  private List<Thing> things;

  @Override
  public void startAsyncLoad() {
    new LoadThread().start();
  }

  @Override
  public List<Thing> getThings() {
    if (things==null) {
      things=new ArrayList<>();

      for (Question item : rawResult.items) {
        things.add(item);
      }
    }

    return(things);
  }

  private class LoadThread extends Thread {
    static final String SO_URL=
        "https://api.stackexchange.com/2.1/questions?"
            + "order=desc&sort=creation&site=stackoverflow&tagged=android";

    @Override
    public void run() {
      try {
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(SO_URL).build();
        Response response=client.newCall(request).execute();

        if (response.isSuccessful()) {
          Reader in=response.body().charStream();
          BufferedReader reader=new BufferedReader(in);

          rawResult=new Gson().fromJson(reader, SOQuestions.class);
          reader.close();

          EventBus
            .getDefault()
            .post(new ThingsLoadedEvent(QuestionsLoader.this));
        }
        else {
          Log.e(getClass().getSimpleName(), response.toString());
        }
      }
      catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception parsing JSON", e);
      }
    }
  }
}
