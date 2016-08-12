package com.parse.starter;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestObject extends JSONObject {
/* Note is the description the user give, the details of the request*/
    //This class is used to display request natively

    public RequestObject(JSONObject base){
        try {
            this.setAddr(base.getString("address"));
            this.setCate(base.getString("category"));
            this.setLocationLat(base.getDouble("latitude"));
            this.setLocationLong(base.getDouble("longitude"));
            this.setRadius(base.getInt("rad"));
            this.setUser(base.getString("username"));
            this.setNote(base.getString("note"));
            this.setPurpose(base.getString("purpose"));
            this.setReward(base.getString("reward"));
            this.setRating(base.getString("rating"));
            this.setUserPic(base.getString("userpic"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPurpose() {
        try {
            return getString("purpose");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setPurpose(String value) {
        try {
            put("purpose", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getRating() {
        try {
            return getString("rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setRating(String value) {
        try {
            put("rating", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getNote() {
      try {
          return getString("note");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public void setNote(String value) {
      try {
          put("note", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String getAddr() {
      try {
          return getString("addr");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public void setAddr(String value) {
      try {
          put("addr", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String getCate() {
      try {
          return getString("cate");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public void setCate(String value) {
      try {
          put("cate", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String getReward() {
        try {
            return getString("reward");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setReward(String value) {
        try {
            put("reward", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getRadius() {
      try {
          return getInt("radius");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return 0;
    }

    public void setRadius(int value) {
      try {
          put("radius", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String getUser() {
      try {
          return getString("username");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return null;
    }

    public void setUser(String value) {
      try {
          put("username", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }


    public String getUserPic() {
        try {
            return getString("userpic");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setUserPic(String value) {
        try {
            put("userpic", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLocationLat() {
        return optString("latitude", "43.6628917");
    }

    public void setLocationLat(double value) {
      try {
          put("latitude", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String getLocationLong() {
        return optString("longitude", "-79.3956564");
    }

    public void setLocationLong(double value) {
      try {
          put("longitude", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public String[] spitValueList(){
        String[] list = new String[]{getUser(), getCate(), getNote(), getReward(), getRating(), getLocationLat(), getLocationLong()};

        return list;
    }

}
