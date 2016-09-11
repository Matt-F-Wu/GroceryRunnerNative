package com.parse.favourama;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestObject{
/* Note is the description the user give, the details of the request*/
    //This class is used to display request natively
    JSONObject jsonObject;

    public RequestObject(JSONObject base){
        jsonObject = base;
    }

    public String getPurpose() {
        try {
            return jsonObject.getString("purpose");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "ask";
    }

    public String getRating() {
        try {
            return jsonObject.getString("rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNote() {
      try {
          return jsonObject.getString("note");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public String getCate() {
      try {
          return jsonObject.getString("cate");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public String getReward() {
        try {
            return jsonObject.getString("reward");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getRadius() {
      try {
          return jsonObject.getInt("radius");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return 0;
    }

    public String getUser() {
      try {
          return jsonObject.getString("username");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public String getUserPic() {
        try {
            return jsonObject.getString("userpic");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTime(){
        return String.valueOf(jsonObject.optLong("time", System.currentTimeMillis()));
    }

    public String getLocationLat() {
        return jsonObject.optString("latitude", "43.6628917");
    }

    public String getLocationLong() {
        return jsonObject.optString("longitude", "-79.3956564");
    }

    public String[] spitValueList(){
        String[] list = new String[]{getUser(), getCate(), getNote(), getReward(), getRating(), getLocationLat(), getLocationLong(), getTime()};

        return list;
    }

}
