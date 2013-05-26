package be.tfe.android.misc.utils;

import java.util.ArrayList;

import be.tfe.android.R;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.Button;

public class ButtonGroup {
	private ArrayList<Button> buttons;
	private Drawable backgroundRect;
	private ArrayList<Drawable> defaultBackgrounds;
	public ButtonGroup(Resources res)
	{
		buttons = new ArrayList<Button>();
		backgroundRect = res.getDrawable(R.drawable.rectangle);
		defaultBackgrounds = new ArrayList<Drawable>();
	}
	
	public void addButton(Button b)
	{
		defaultBackgrounds.add(b.getBackground());
		if(buttons.size() == 0)
			selectButton(b);
		buttons.add(b);
	}
	
	public void selectButton(int j)
	{
		for(int i = 0 ; i < buttons.size() ; i++)
			if(i == j)
				selectButton(buttons.get(i));
			else
				buttons.get(i).setBackgroundDrawable(defaultBackgrounds.get(i));
	}
	
	public void selectButton(Button b)
	{
		b.setBackgroundDrawable(backgroundRect);
	}
}
