package ust.csit5110.handpose.util;

import java.util.jar.Attributes;

import android.graphics.Bitmap;
import ust.csit5110.handpose.surf.*;

public class DataWareHouse {
	
	// attribute store
	private Attributes _attribute;
	
	public void SetAttribute(String key, String value){
		_attribute.putValue(key, value);
	}
	
	public String GetAttribute(String key){
		return _attribute.getValue(key);
	}
	
	public void ClearAttribute(){
		_attribute.clear();
	}
	
	// template store
	private Surf _templateData;
	private Bitmap _templateBitmap;
	private int _templatePointCount;
	
	public void SetTemplate(Bitmap input){
		Surf newSurf = new Surf(input);
		_templateData = newSurf;
		_templateBitmap = input;
		_templatePointCount = newSurf.getUprightInterestPoints().size();
	}
	
	public Surf GetTemplate(){
		return _templateData;
	}
	
	public Bitmap GetTemplateBitmap(){
		return _templateBitmap;
	}
	
	public int GetTemplatePointCount(){
		return _templatePointCount;
	}
	
	// public accessor
	public static DataWareHouse Instance(){
		if( _instance == null ){
			_instance = new DataWareHouse();
		}
		return _instance;
	}

	// private members
	private static DataWareHouse _instance;
	
	// private constructor
	private DataWareHouse(){
		_attribute = new Attributes();
		_attribute.clear();
	}
}
