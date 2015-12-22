package com.parse.starter;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestObject extends JSONObject {
/* Note is the description the user give, the details of the request*/
    //This class is used to display request natively

    public RequestObject RequestObject(JSONObject base){
        RequestObject obj = new RequestObject();
        try {
            obj.setAddr(base.getString("address"));
            obj.setCate(base.getString("category"));
            obj.setLocationLat(base.getDouble("latitude"));
            obj.setLocationLong(base.getDouble("longitude"));
            obj.setRadius(base.getInt("rad"));
            obj.setUser(base.getString("username"));
            obj.setNote(base.getString("note"));
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
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
          put("user", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public double getLocationLat() {
      try {
          return getDouble("latitude");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return 0;
    }

    public void setLocationLat(double value) {
      try {
          put("latitude", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }

    public double getLocationLong() {
      try {
          return getDouble("longitude");
      } catch (JSONException e) {
          e.printStackTrace();
      }
      return 0;
    }

    public void setLocationLong(double value) {
      try {
          put("longitude", value);
      } catch (JSONException e) {
          e.printStackTrace();
      }
    }
}
