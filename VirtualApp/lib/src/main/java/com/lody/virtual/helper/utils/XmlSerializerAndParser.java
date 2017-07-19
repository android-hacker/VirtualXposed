package com.lody.virtual.helper.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public interface XmlSerializerAndParser<T> {
    void writeAsXml(T item, XmlSerializer out) throws IOException;
    T createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException;
}