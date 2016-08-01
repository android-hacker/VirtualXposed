package com.lody.virtual.client.filter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Class:
 * Created by andy on 16-8-1.
 * TODO:
 */
public class BaseContentProvider extends ContentProvider {
  public static Context context;

  @Override
  public boolean onCreate() {
    context = getContext();
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return null;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  public static Builder Builder(Context context, String author, String method, String args, Bundle bundle) {
    return new Builder(context, author, method, args, bundle);
  }

  public static Builder Builder() {
    return new Builder();
  }


  public static class Builder {
    private Context context;
    private String author;
    private String method;
    private String args;
    private Bundle bundle;

    public Builder(Context context, String author, String method, String args, Bundle bundle) {
      this.context = context;
      this.author = author;
      this.method = method;
      this.args = args;
      this.bundle = bundle;
    }

    public Builder() {
    }

    public Builder context(Context context) {
      this.context = context;
      return this;
    }

    public Builder author(String author) {
      this.author = author;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder args(String args) {
      this.args = args;
      return this;
    }

    public Builder bundle(Bundle bundle) {
      this.bundle = bundle;
      return this;
    }

    public Bundle call() {
      return call(this.bundle);
    }

    public Bundle call(Bundle bundle) {
      if (context != null)
        return context.getContentResolver().call(Uri.parse("content://" + this.author), method, this.args, bundle);
      else
        return null;
    }
  }
}
