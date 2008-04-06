////////////////////////////////////////////////////////////////////////////////
// TunnelX -- Cave Drawing Program
// Copyright (C) 2002  Julian Todd.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////
package Tunnel;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;

import java.awt.Insets;
//import java.lang.NumberFormatException;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Component;

import javax.swing.border.Border;
import javax.swing.BorderFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusListener;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Color; 

import java.io.IOException;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JCheckBoxMenuItem;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import javax.swing.text.BadLocationException;
//
//
// SketchLineStyle
//
//

/////////////////////////////////////////////
class SketchLineStyle extends JPanel
{
	// parallel arrays of wall style info.
	static String[] linestylenames = { "Centreline", "Wall", "Est. Wall", "Pitch Bound", "Ceiling Bound", "Detail", "Invisible", "Connective", "Filled" };
	static String[] shortlinestylenames = { "Cent", "Wall", "EstW", "Pitc", "CeilB", "Detl", "Invs", "Conn", "Fill" };
	static final int SLS_CENTRELINE = 0;

	static final int SLS_WALL = 1;
	static final int SLS_ESTWALL = 2;

	static final int SLS_PITCHBOUND = 3;
	static final int SLS_CEILINGBOUND = 4;

	static final int SLS_DETAIL = 5;
	static final int SLS_INVISIBLE = 6;
	static final int SLS_CONNECTIVE = 7;
	static final int SLS_FILLED = 8;

	static final int SLS_SYMBOLOUTLINE = 9; // not a selected style.

	static float strokew = -1.0F;

	static Color fontcol = new Color(0.7F, 0.3F, 1.0F);
	static LabelFontAttr stationPropertyFontAttr = null;
	static Font defaultfontlab = null;

	// area-connective type signals which get loaded and their numeric values
	static final int ASE_KEEPAREA = 0;		// default state
	static final int ASE_VERYSTEEP = 0;		// not used yet, but will define an area that's a foreshortened pitch wall
	static final int ASE_HCOINCIDE = 1;		// pitch dropdown connection (on paths, not areas)
	static final int ASE_OUTLINEAREA = 2;	// pitch hole
	static final int ASE_KILLAREA = 3;		// column
	static final int ASE_ZSETRELATIVE = 5;	// setting relative z displacement between the nodes (on paths, not areas)
	static final int ASE_ELEVATIONPATH = 6;	// defines the connective as forming the path of an elevation diagramette
	static final int ASE_OUTERAREA = 7;		// assigned to an outer area of the diagram (not selectable)
	static final int ASE_NOAREA = 8;		// assigned to the object when path is part of a tree (not selectable)
	static final int ASE_SKETCHFRAME = 55;	// defining the interior of a frame

	static String[] areasignames = new String[10];
	static int[] areasigeffect = new int[10];
	static int iareasigelev = -1; 
	static int nareasignames = 0;

	//Colours for drawing symbols
	private static Color linestylesymbcol = new Color(0.0F, 0.1F, 0.8F);
	private static Color linestylesymbcolinvalid = new Color(0.3F, 0.3F, 0.6F, 0.77F);

	//Line style used as a border for printing to help it to be cut out
	static LineStyleAttr printcutoutlinestyleattr = null;
	//Line styles for drawing paths when not in detail mode
	static Color linestylecolactive = Color.magenta;
	static LineStyleAttr[] ActiveLineStyleAttrs = new LineStyleAttr[10];
	static float mouperplinlength; 
	private static Color[] inSelSubsetColors = {Color.red, Color.blue, Color.blue, new Color(0.7F, 0.0F, 1.0F), Color.cyan, Color.blue, new Color(0.0F, 0.9F, 0.0F), new Color(0.5F, 0.8F, 0.0F), Color.black, Color.black};
	static LineStyleAttr[] inSelSubsetLineStyleAttrs = new LineStyleAttr[10];
	static Color notInSelSubsetCol = new Color(0.6F, 0.6F, 0.9F);
	static Color blankbackimagecol = new Color(0.9F, 0.9F, 0.6F);
	static LineStyleAttr[] notInSelSubsetLineStyleAttrs = new LineStyleAttr[10];
	//Line styles for drawing nodes
	static LineStyleAttr pnlinestyleattr = null;
	static LineStyleAttr activepnlinestyleattr = null;
	static LineStyleAttr firstselpnlinestyleattr = null;
	static LineStyleAttr lastselpnlinestyleattr = null;
	static LineStyleAttr middleselpnlinestyleattr = null;
	//Lines for drawing symbols to screen
	static LineStyleAttr linestylesymb = null;
	static LineStyleAttr linestylefirstsymb = null;
	static LineStyleAttr linestylesymbinvalid = null;
	static LineStyleAttr linestylefirstsymbinvalid = null;
	static LineStyleAttr lineactivestylesymb = null;
	static LineStyleAttr fillstylesymb = null;
	static LineStyleAttr fillstylefirstsymb = null;
	static LineStyleAttr fillstylesymbinvalid = null;
	static LineStyleAttr fillstylefirstsymbinvalid = null;
	static LineStyleAttr fillactivestylesymb = null;
	//Lines for hatching areas
	static LineStyleAttr linestylehatch1 = null;
	static LineStyleAttr linestylehatch2 = null;

	static String[] linestylebuttonnames = { "", "W", "E", "P", "C", "D", "I", "N", "F" };
	static int[] linestylekeystrokes = { 0, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_P, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_I, KeyEvent.VK_N, KeyEvent.VK_F };

	//These should be removed eventually...
	static BasicStroke gridStroke = null;
	static Color gridColor = null;

	// used to get back for defaults and to the active path.
	SketchDisplay sketchdisplay;

	// (we must prevent the centreline style from being selected --  it's special).
	JComboBox linestylesel = new JComboBox(linestylenames);
	JToggleButton pthsplined = new JToggleButton("s");

	// tabbing panes that are put in the bottom part
	CardLayout pthstylecardlayout = new CardLayout();
	JPanel pthstylecards = new JPanel(pthstylecardlayout);
	String pthstylecardlayoutshown = null;

	// a panel displayed when no path is selected (useful for holding a few spare buttons)
	JPanel pthstylenonconn = new JPanel();

	// panel of deselect and delete buttons
	JPanel pathcoms = new JPanel(new GridLayout(1, 0));

	// the other panel types
	ConnectiveCentrelineTabPane pthstylecentreline = new ConnectiveCentrelineTabPane();
	ConnectiveLabelTabPane pthstylelabeltab = new ConnectiveLabelTabPane();
	ConnectiveAreaSigTabPane pthstyleareasigtab;

	SymbolsDisplay symbolsdisplay; // a tabbed pane


	// secondary sets of colours which over-ride using the icolindex attribute in lines
	static Color[] linestylecolsindex = new Color[100];
	static Color[] areastylecolsindex = new Color[200];

// this will be a list
	/////////////////////////////////////////////
	Map<String, SubsetAttrStyle> subsetattrstylesmap = new TreeMap<String, SubsetAttrStyle>(); 
	boolean bsubsetattributesneedupdating = false;
	SubsetAttrStyle GetSubsetAttrStyle(String sasname) // dead func
	{
		// find the upper default we inherit from
		if (sasname == null)
			return null;
		return subsetattrstylesmap.get(sasname); 
	}






	/////////////////////////////////////////////
	public class AclsButt extends AbstractAction
	{
		int index;
	    public AclsButt(int lindex)
		{
			super(linestylebuttonnames[lindex]);
			index = lindex;
            putValue(SHORT_DESCRIPTION, linestylenames[index]);
            putValue(MNEMONIC_KEY, new Integer(linestylekeystrokes[index]));
		}

	    public void actionPerformed(ActionEvent e)
		{
			linestylesel.setSelectedIndex(index);
		}
	}

	/////////////////////////////////////////////
	class LineStyleButton extends JButton
	{
		int index;

		LineStyleButton(int lindex)
		{
			super(new AclsButt(lindex));
			index = lindex;
			setMargin(new Insets(2, 2, 2, 2));
		}
	};




	/////////////////////////////////////////////
	static void SetStrokeWidths(float lstrokew)
	{
		strokew = lstrokew;
		//TN.emitMessage("New stroke width: " + strokew);

		float[] dash = new float[2];

		//Set Grid stroke and colour
		gridStroke = new BasicStroke(1.0F * strokew);
		gridColor = Color.black;
		//Set Line style attributes for the cut out line when printing
		printcutoutlinestyleattr = new LineStyleAttr(SLS_SYMBOLOUTLINE, 3.0F * strokew, 6 * 2, 4 * 2, 0, Color.lightGray);
		//Set Line style attributes for non active path nodes
		pnlinestyleattr = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, Color.blue);
		//Set Line style attributes for active path nodes
		activepnlinestyleattr = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, Color.magenta);
		//Set Line style attributes for the first selected path nodes
		firstselpnlinestyleattr = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, new Color(1.0F, 0.5F, 1.0F));
		//Set Line style attributes for the last selected path nodes
		lastselpnlinestyleattr = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, new Color(0.8F, 0.0F, 0.8F));
		//Set Line style attributes for the middle selected path nodes
		middleselpnlinestyleattr = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, Color.magenta);

		//Set Line style attributes for hatching areas
		linestylehatch1 = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, Color.blue);
		linestylehatch2 = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, Color.cyan);

		//Set default font
		stationPropertyFontAttr = new LabelFontAttr(fontcol, new Font("Serif", 0, Math.max(4, (int)(strokew * 15))));

		//Lines for drawing symbols to screen
		linestylesymb = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, linestylesymbcol);
		linestylesymbinvalid = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, linestylesymbcolinvalid);
		lineactivestylesymb = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, linestylecolactive);
		fillstylesymb = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, linestylesymbcol);
		fillstylesymbinvalid = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, linestylesymbcolinvalid);
		fillactivestylesymb = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, linestylecolactive);


		// set 'in selected subsets' line style attributes
		inSelSubsetLineStyleAttrs[SLS_CENTRELINE] = new LineStyleAttr(SLS_CENTRELINE, 0.5F * strokew, 0, 0, 0, inSelSubsetColors[0]);
		inSelSubsetLineStyleAttrs[SLS_WALL] = new LineStyleAttr(SLS_WALL, 2.0F * strokew, 0, 0, 0, inSelSubsetColors[1]);
		inSelSubsetLineStyleAttrs[SLS_ESTWALL] = new LineStyleAttr(SLS_ESTWALL, 2.0F * strokew, 12 * strokew, 6 * strokew, 0, inSelSubsetColors[2]);
		inSelSubsetLineStyleAttrs[SLS_PITCHBOUND] = new LineStyleAttr(SLS_PITCHBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, inSelSubsetColors[3]);
		inSelSubsetLineStyleAttrs[SLS_CEILINGBOUND] = new LineStyleAttr(SLS_CEILINGBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, inSelSubsetColors[4]);
		inSelSubsetLineStyleAttrs[SLS_DETAIL] = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, inSelSubsetColors[5]);
		inSelSubsetLineStyleAttrs[SLS_INVISIBLE] = new LineStyleAttr(SLS_INVISIBLE, 1.0F * strokew, 0, 0, 0, inSelSubsetColors[6]);
		inSelSubsetLineStyleAttrs[SLS_CONNECTIVE] = new LineStyleAttr(SLS_CONNECTIVE, 1.0F * strokew, 6 * strokew, 3 * strokew, 0, inSelSubsetColors[7]);
		inSelSubsetLineStyleAttrs[SLS_FILLED] = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, inSelSubsetColors[8]);
		// symbol paint background.
		inSelSubsetLineStyleAttrs[SLS_SYMBOLOUTLINE] = new LineStyleAttr(SLS_SYMBOLOUTLINE, 3.0F * strokew, 0, 0, 0, inSelSubsetColors[9]);// for printing.

		// set 'active (highlighted)' line style attributes
		ActiveLineStyleAttrs[SLS_CENTRELINE] = new LineStyleAttr(SLS_CENTRELINE, 0.5F * strokew, 0, 0, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_WALL] = new LineStyleAttr(SLS_WALL, 2.0F * strokew, 0, 0, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_ESTWALL] = new LineStyleAttr(SLS_ESTWALL, 2.0F * strokew, 12 * strokew, 6 * strokew, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_PITCHBOUND] = new LineStyleAttr(SLS_PITCHBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_CEILINGBOUND] = new LineStyleAttr(SLS_CEILINGBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_DETAIL] = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_INVISIBLE] = new LineStyleAttr(SLS_INVISIBLE, 1.0F * strokew, 0, 0, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_CONNECTIVE] = new LineStyleAttr(SLS_CONNECTIVE, 1.0F * strokew, 6 * strokew, 3 * strokew, 0, linestylecolactive);
		ActiveLineStyleAttrs[SLS_FILLED] = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, linestylecolactive);
		// symbol paint background.
		ActiveLineStyleAttrs[SLS_SYMBOLOUTLINE] = new LineStyleAttr(SLS_SYMBOLOUTLINE, 3.0F * strokew, 0, 0, 0, Color.white);// for printing.

		mouperplinlength = 8 * strokew; 

		// set 'not in selected subsets' line style attributes
		notInSelSubsetLineStyleAttrs[SLS_CENTRELINE] = new LineStyleAttr(SLS_CENTRELINE, 0.5F * strokew, 0, 0, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_WALL] = new LineStyleAttr(SLS_WALL, 2.0F * strokew, 0, 0, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_ESTWALL] = new LineStyleAttr(SLS_ESTWALL, 2.0F * strokew, 12 * strokew, 6 * strokew, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_PITCHBOUND] = new LineStyleAttr(SLS_PITCHBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_CEILINGBOUND] = new LineStyleAttr(SLS_CEILINGBOUND, 1.0F * strokew, 16 * strokew, 6 * strokew, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_DETAIL] = new LineStyleAttr(SLS_DETAIL, 1.0F * strokew, 0, 0, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_INVISIBLE] = new LineStyleAttr(SLS_INVISIBLE, 1.0F * strokew, 0, 0, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_CONNECTIVE] = new LineStyleAttr(SLS_CONNECTIVE, 1.0F * strokew, 6 * strokew, 3 * strokew, 0, notInSelSubsetCol);
		notInSelSubsetLineStyleAttrs[SLS_FILLED] = new LineStyleAttr(SLS_FILLED, 0.0F * strokew, 0, 0, 0, notInSelSubsetCol);
		// symbol paint background.
		notInSelSubsetLineStyleAttrs[SLS_SYMBOLOUTLINE] = new LineStyleAttr(SLS_SYMBOLOUTLINE, 3.0F * strokew, 0, 0, 0, notInSelSubsetCol);// for printing.
	}


	// this is dangerous but seems to work.
	boolean bsettingaction = false;

	/////////////////////////////////////////////
	// we don't otherwise have a way to recover which card is visible
	void Showpthstylecard(String lpthstylecardlayoutshown)
	{
		pthstylecardlayoutshown = lpthstylecardlayoutshown;
		pthstylecardlayout.show(pthstylecards, pthstylecardlayoutshown);
	}

	/////////////////////////////////////////////
	void SetClearedTabs(String tstring, boolean benableconnbuttons)
	{
		sketchdisplay.SetEnabledConnectiveSubtype(benableconnbuttons);
		Showpthstylecard(tstring);

		// zero the other visual areas
		pthstylelabeltab.labtextfield.setText("");
		pthstylelabeltab.setTextPosCoords(-1, -1);
		pthstylelabeltab.jcbarrowpresent.setSelected(false);
		pthstylelabeltab.jcbboxpresent.setSelected(false);
		LSpecSymbol(true, null);
		//?? if (!FileAbstraction.bIsApplet)  // can't handle this
		{
			pthstyleareasigtab.areasignals.setSelectedIndex(0);
			pthstyleareasigtab.SetFrameSketchInfoText(null);
		}
	}


	/////////////////////////////////////////////
	boolean SetFrameZSetRelative(OnePath op)
	{
		float pnodeconnzsetrelative = op.plabedl.nodeconnzsetrelative;
		try
		{
		op.plabedl.nodeconnzsetrelative = Float.parseFloat(pthstyleareasigtab.tfsubmapping.getText().trim());
		}
		catch (NumberFormatException e)  { System.out.println(pthstyleareasigtab.tfsubmapping.getText()); };
		return (pnodeconnzsetrelative != op.plabedl.nodeconnzsetrelative);
	}



	/////////////////////////////////////////////
	// this has got two uses; when we select a new path,
	// or we change the linestyle of a path
	boolean SetConnectiveParametersIntoBoxes(OnePath op)
	{
		if (op == null)
		{
			bsettingaction = true;
			SetClearedTabs("Nonconn", false);
			bsettingaction = false;
			return false;
		}

		bsettingaction = true;
		op.linestyle = linestylesel.getSelectedIndex(); // this would recopy it just after it had been copied over, I guess

		if (op.linestyle == SLS_CONNECTIVE)
		{
			// symbols present in this one
			if ((op.plabedl != null) && !op.plabedl.vlabsymb.isEmpty())
			{
				Showpthstylecard("Symbol");
				symbolsdisplay.SelEnableButtons(op.subsetattr);
				symbolsdisplay.UpdateSymbList(op.plabedl.vlabsymb, op.subsetattr);
			}

			// label type at this one
			else if ((op.plabedl != null) && (op.plabedl.sfontcode != null))
			{
				pthstylelabeltab.fontstyles.setSelectedIndex(pthstylelabeltab.lfontstyles.indexOf(op.plabedl.sfontcode));
				pthstylelabeltab.setTextPosCoords(op.plabedl.fnodeposxrel, op.plabedl.fnodeposyrel);
				pthstylelabeltab.jcbarrowpresent.setSelected(op.plabedl.barrowpresent);
				pthstylelabeltab.jcbboxpresent.setSelected(op.plabedl.bboxpresent);
				pthstylelabeltab.labtextfield.setText(op.plabedl.drawlab == null ? "" : op.plabedl.drawlab);
				Showpthstylecard("Label");
				pthstylelabeltab.labtextfield.requestFocus();
			}

			// area-signal present at this one
			else if ((op.plabedl != null) && (op.plabedl.iarea_pres_signal != 0))
			{
				pthstyleareasigtab.areasignals.setSelectedIndex(op.plabedl.iarea_pres_signal);
				pthstyleareasigtab.SetFrameSketchInfoText(op);
				Showpthstylecard("Area-Sig");
			}

			// none specified; free choice
			else
				SetClearedTabs("Conn", true);
		}
		else if (op.linestyle == SLS_CENTRELINE)
		{
			pthstylecentreline.tfhead.setText(((op.plabedl != null) && (op.plabedl.centrelinehead != null)) ? op.plabedl.centrelinehead : "--nothing--");
			pthstylecentreline.tftail.setText(((op.plabedl != null) && (op.plabedl.centrelinetail != null)) ? op.plabedl.centrelinetail : "--nothing--");
			pthstylecentreline.tfelev.setText(((op.plabedl != null) && (op.plabedl.centrelineelev != null)) ? op.plabedl.centrelineelev : "");
			Showpthstylecard("Centreline");
		}
		else
		{
			sketchdisplay.SetEnabledConnectiveSubtype(false);
			Showpthstylecard("Nonconn");
		}

		bsettingaction = false;
		return true;
	}

	/////////////////////////////////////////////
	// we have some confounding situations of what to show when there is no path shown
	void SetParametersIntoBoxes(OnePath op)
	{
		bsettingaction = true;
		if (op != null)
		{
			bsettingaction = true;
			pthsplined.setSelected(op.bWantSplined);
			linestylesel.setSelectedIndex(op.linestyle);
		}

		// null case
		else
		{
			if (linestylesel.getSelectedIndex() == SLS_CENTRELINE)
				linestylesel.setSelectedIndex(SLS_DETAIL);

			// set the splining by default.
			// except make the splining off if the type is connective, which we don't really want splined since it's distracting.
//			pthsplined.setSelected(sketchdisplay.miDefaultSplines.isSelected() && (linestylesel.getSelectedIndex() != SLS_CONNECTIVE));
			bsettingaction = false;

			sketchdisplay.SetEnabledConnectiveSubtype(false);
			Showpthstylecard("Nonconn");
		}
		bsettingaction = false;

		// we have a connective type, so should load the contents here
		SetConnectiveParametersIntoBoxes(op);
	}



	/////////////////////////////////////////////
	void GoSetParametersCurrPath()  // this calls function below
	{
		OnePath op = sketchdisplay.sketchgraphicspanel.currgenpath;
		if ((op == null) || !sketchdisplay.sketchgraphicspanel.bEditable)
			return;

		// if the spline changes then the area should change too.
		if (SetParametersFromBoxes(op))
		{
			sketchdisplay.sketchgraphicspanel.RedrawBackgroundView();
			sketchdisplay.sketchgraphicspanel.SketchChanged(SketchGraphics.SC_CHANGE_STRUCTURE);
		}
	}


	/////////////////////////////////////////////
	// returns true if anything actually changed.
	boolean SetParametersFromBoxes(OnePath op)
	{
		boolean bRes = false;

		int llinestyle = linestylesel.getSelectedIndex();
		bRes |= (op.linestyle != llinestyle);
		op.linestyle = llinestyle;

		bRes |= (op.bWantSplined != pthsplined.isSelected());
		op.bWantSplined = pthsplined.isSelected();

		// go and spline it if required
		// (should do this in the redraw actually).
		if ((op.pnend != null) && (op.bWantSplined != op.bSplined))
			op.Spline(op.bWantSplined && !OnePath.bHideSplines, false);

		// we have a connective type, so should load the contents here
		if (op.plabedl != null)
		{
			// symbols are loaded as they are pressed.
			//if ((op.plabedl != null) && !op.plabedl.vlabsymb.isEmpty())
			//	pthstylecardlayout.show(pthstylecards, "Symbol");

			// label type at this one
			if (pthstylecardlayoutshown.equals("Label"))
			{
				String ldrawlab = pthstylelabeltab.labtextfield.getText().trim();
				int lifontcode = pthstylelabeltab.fontstyles.getSelectedIndex();

				String lsfontcode = (lifontcode == -1 ? "default" : pthstylelabeltab.lfontstyles.get(lifontcode));
				if ((op.plabedl.drawlab == null) || !op.plabedl.drawlab.equals(ldrawlab) || (op.plabedl.sfontcode == null) || (!op.plabedl.sfontcode.equals(lsfontcode)))
				{
					op.plabedl.drawlab = ldrawlab;
					op.plabedl.sfontcode = lsfontcode;
					bRes = true;
				}

				float pfnodeposxrel = op.plabedl.fnodeposxrel;
				float pfnodeposyrel = op.plabedl.fnodeposyrel;
				boolean pbarrowpresent = op.plabedl.barrowpresent;
				boolean pbboxpresent = op.plabedl.bboxpresent;
				try
				{
				op.plabedl.fnodeposxrel = Float.parseFloat(pthstylelabeltab.tfxrel.getText());
				op.plabedl.fnodeposyrel = Float.parseFloat(pthstylelabeltab.tfyrel.getText());
				} catch (NumberFormatException e)  { System.out.println(pthstylelabeltab.tfxrel.getText() + "/" + pthstylelabeltab.tfyrel.getText()); };
				op.plabedl.barrowpresent = pthstylelabeltab.jcbarrowpresent.isSelected();
				op.plabedl.bboxpresent = pthstylelabeltab.jcbboxpresent.isSelected();

				if ((pfnodeposxrel != op.plabedl.fnodeposxrel) || (pfnodeposyrel != op.plabedl.fnodeposyrel) || (pbarrowpresent != op.plabedl.barrowpresent) || (pbboxpresent != op.plabedl.bboxpresent))
					bRes = true;
			}
			else
			{
				bRes = ((op.plabedl.drawlab != null) || (op.plabedl.sfontcode != null));
				op.plabedl.drawlab = null;
				op.plabedl.sfontcode = null;
			}


			// area-signal present at this one (no need to specialize because default is number 0)
			// if (pthstylecardlayoutshown.equals("Area-sig"))
			int liarea_pres_signal = pthstyleareasigtab.areasignals.getSelectedIndex();
			if (op.plabedl.iarea_pres_signal != liarea_pres_signal)
			{
				op.plabedl.iarea_pres_signal = liarea_pres_signal;  // look up in combobox
				int bareapre = op.plabedl.barea_pres_signal;
				op.plabedl.barea_pres_signal = areasigeffect[op.plabedl.iarea_pres_signal];

				// change in state.  update
				pthstyleareasigtab.SetFrameSketchInfoText(op);
				bRes = true;
			}

			if (op.plabedl.barea_pres_signal == SketchLineStyle.ASE_ZSETRELATIVE)
				bRes |= SetFrameZSetRelative(op);
		}

		op.SetSubsetAttrs(sketchdisplay.subsetpanel.sascurrent, sketchdisplay.vgsymbols, sketchdisplay.sketchlinestyle.pthstyleareasigtab.sketchframedefCopied); // font change
		return bRes;
	}


	/////////////////////////////////////////////
	void SetConnTabPane(String tstring)
	{
		OnePath op = sketchdisplay.sketchgraphicspanel.currgenpath;
		if ((op == null) || !sketchdisplay.sketchgraphicspanel.bEditable)
		{
			TN.emitWarning("Must have connective path selected"); // maybe use disabled buttons
			return;
		}
		Showpthstylecard(tstring);
		if (op.plabedl == null)
			op.plabedl = new PathLabelDecode();

		if (tstring.equals("Label"))
			pthstylelabeltab.labtextfield.requestFocus();
		else if (tstring.equals("Symbol"))
			symbolsdisplay.SelEnableButtons(op.subsetattr);
	}

	/////////////////////////////////////////////
	void CopySketchFrameImage()
	{
		if (!((sketchdisplay.sketchgraphicspanel.currgenpath != null) && sketchdisplay.sketchgraphicspanel.currgenpath.IsSketchFrameConnective()))
		{
			TN.emitWarning("Sketch frame connective path must be selected");
			return;
        }
		if (!sketchdisplay.miShowBackground.isSelected() || (sketchdisplay.sketchgraphicspanel.tsketch.ibackgroundimgnamearrsel == -1))
		{
			TN.emitWarning("Background image must be there");
			return;
		}

		OneSketch tsketch = sketchdisplay.sketchgraphicspanel.tsketch;
		pthstyleareasigtab.CopyBackgroundSketchTransform(tsketch.backgroundimgnamearr.get(tsketch.ibackgroundimgnamearrsel), tsketch.backgimgtransarr.get(tsketch.ibackgroundimgnamearrsel), tsketch.sketchLocOffset);

		sketchdisplay.miShowBackground.doClick();  // deselect the background
	}


	/////////////////////////////////////////////
	class DocAUpdate implements DocumentListener, ActionListener
	{
		public void changedUpdate(DocumentEvent e) 
		{
			//System.out.println("EEE: " + e.toString());
		}
		public void removeUpdate(DocumentEvent e)
		{
			//System.out.println("EEE: " + e.toString());
			if (!bsettingaction)
			{
				if (e.getOffset() == 0)  // update when entire thing disappears
					GoSetParametersCurrPath();
			}
		}
		public void insertUpdate(DocumentEvent e)
		{
			//System.out.println("EEE: " + e.toString());
			if (!bsettingaction)
			{
				// update when space is pressed
				try {
					String istr = e.getDocument().getText(e.getOffset(), e.getLength());
					if ((istr.indexOf(' ') != -1) || (istr.indexOf('\n') != -1) || (istr.indexOf('%') != -1))
						GoSetParametersCurrPath();
				} catch (BadLocationException ex) {;};
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			//System.out.println("EEE: " + e.toString());
			if (!bsettingaction)
				GoSetParametersCurrPath();
		}
	};



	/////////////////////////////////////////////
	// from when the symbol buttons are pressed
	boolean LSpecSymbol(boolean bOverwrite, String name)
	{
		// shares much code from GoSetParametersCurrPath
		OnePath op = sketchdisplay.sketchgraphicspanel.currgenpath;
		if ((op == null) || !sketchdisplay.sketchgraphicspanel.bEditable)
			return false;

		if ((op.linestyle != SLS_CONNECTIVE) || (op.plabedl == null))
			return false;

    		assert ((name != null) || bOverwrite);
		if ((name == null) && op.plabedl.vlabsymb.isEmpty())
			return false; // no change

   		if (bOverwrite)
			op.plabedl.vlabsymb.clear();
		if (name != null)
			op.plabedl.vlabsymb.add(name);
		symbolsdisplay.UpdateSymbList(op.plabedl.vlabsymb, op.subsetattr);


		sketchdisplay.sketchgraphicspanel.SketchChanged(SketchGraphics.SC_CHANGE_SYMBOLS);
		op.GenerateSymbolsFromPath(sketchdisplay.vgsymbols);
		sketchdisplay.sketchgraphicspanel.RedrawBackgroundView();
		return true;
	}

	/////////////////////////////////////////////
	void SetupSymbolStyleAttr()
	{
		// apply a setup on all the symbols in the attribute styles
		for (SubsetAttrStyle sas : subsetattrstylesmap.values())
		{
			for (SubsetAttr sa : sas.msubsets.values())
			{
				for (SymbolStyleAttr ssa : sa.subautsymbolsmap.values())
					ssa.SetUp(sketchdisplay.vgsymbols);
			}
		}
	}

	/////////////////////////////////////////////
	SketchLineStyle(SymbolsDisplay lsymbolsdisplay, SketchDisplay lsketchdisplay)
	{
		symbolsdisplay = lsymbolsdisplay;
		sketchdisplay = lsketchdisplay;
		pthstyleareasigtab = new ConnectiveAreaSigTabPane(this);

		setBackground(TN.sketchlinestyle_col);

		Border bord_loweredbevel = BorderFactory.createLoweredBevelBorder();
		Border bord_redline = BorderFactory.createLineBorder(Color.blue);
		Border bord_compound = BorderFactory.createCompoundBorder(bord_redline, bord_loweredbevel);
		pthstylecards.setBorder(bord_compound);


		// do the button panel
		JPanel buttpanel = new JPanel();
		buttpanel.setLayout(new GridLayout(1, 0));
		Insets inset = new Insets(1, 1, 1, 1);
		for (int i = 0; i < linestylebuttonnames.length; i++)
		{
			if (!linestylebuttonnames[i].equals(""))
			{
				buttpanel.add(new LineStyleButton(i));
				pthsplined.setMargin(inset);
  			}
		}
		pthsplined.setMargin(new Insets(5, 5, 5, 5));
		buttpanel.add(pthsplined);
		linestylesel.setSelectedIndex(SLS_DETAIL);

		// the listener for all events among the linestyles
		DocAUpdate docaupdate = new DocAUpdate();

		// action listeners on the linestyles
		pthsplined.addActionListener(docaupdate);

		// change of linestyle
		linestylesel.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)
				{
				  if (!bsettingaction)
				  { if (sketchdisplay.miDefaultSplines.isSelected())
						pthsplined.setSelected((linestylesel.getSelectedIndex() != SLS_CONNECTIVE) && (linestylesel.getSelectedIndex() != SLS_CENTRELINE));
					GoSetParametersCurrPath();
					SetConnectiveParametersIntoBoxes(sketchdisplay.sketchgraphicspanel.currgenpath);
				  }
				}
			} );

		// LSpecSymbol calls added with the symbolsdisplay


		// put in the tabbing panes updates
		pthstyleareasigtab.areasignals.addActionListener(docaupdate);
		pthstylelabeltab.fontstyles.addActionListener(docaupdate);
		pthstylelabeltab.tfxrel.addActionListener(docaupdate);
		pthstylelabeltab.tfyrel.addActionListener(docaupdate);
		pthstylelabeltab.jcbarrowpresent.addActionListener(docaupdate);
		pthstylelabeltab.jcbboxpresent.addActionListener(docaupdate);

		pthstylelabeltab.labtextfield.getDocument().addDocumentListener(docaupdate);
		pthstylelabeltab.fontstyles.addActionListener(docaupdate);

		pthstyleareasigtab.tfsubmapping.getDocument().addDocumentListener(docaupdate);

		// cancel buttons
		pthstyleareasigtab.jbcancel.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event) { SetClearedTabs("Nonconn", true);  GoSetParametersCurrPath();  } } );
		pthstylelabeltab.jbcancel.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event) { SetClearedTabs("Nonconn", true);  GoSetParametersCurrPath();  } } );
		symbolsdisplay.jbcancel.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event) { SetClearedTabs("Nonconn", true);  GoSetParametersCurrPath();  } } );

		// the clear symbols button
		symbolsdisplay.jbclear.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event) { LSpecSymbol(true, null);  } } );


		pthstylecards.add(pthstylenonconn, "Nonconn"); // when no connected path is selected
		pthstylecards.add(pthstylecentreline, "Centreline"); // this should have buttons that take you to the other four types
		pthstylecards.add(pthstylelabeltab, "Label");
		pthstylecards.add(symbolsdisplay, "Symbol");
		pthstylecards.add(pthstyleareasigtab, "Area-Sig");


		// do the layout of the main thing.
		JPanel partpanel = new JPanel(new GridLayout(3, 1));
		partpanel.add(linestylesel);
		partpanel.add(buttpanel);
		partpanel.add(pathcoms);  // delete and deselect


		setLayout(new BorderLayout());
		add(partpanel, BorderLayout.NORTH);
		add(pthstylecards, BorderLayout.CENTER);


		// fill in the colour rainbow for showing weighting and depth
		for (int i = 0; i < linestylecolsindex.length; i++)
		{
			float a = (float)i / linestylecolsindex.length ;
			//linestylecolsindex[i] = new Color(Color.HSBtoRGB(0.9F * a, 1.0F, 0.9F));
			linestylecolsindex[i] = new Color(a, (1.0F - a) * 0.2F, 1.0F - a);
		}

		for (int i = 0; i < areastylecolsindex.length; i++)
		{
			float a = (float)i / areastylecolsindex.length ;
			//linestylecolsindex[i] = new Color();
			// fcolw = new Color(0.8F, 1.0F, 1.0F, 0.6F);
			//areastylecolsindex[i] = new Color(0.7F + a * 0.3F, 1.0F - a * 0.3F, 1.0F, 0.6F);
			int col = Color.HSBtoRGB(0.6F * (1.0F - a) + 0.06F, 1.0F, 1.0F) + 0x61000000;
			areastylecolsindex[i] = new Color(col, true);
			//linestylecolsindex[i] = new Color(Color.HSBtoRGB(0.9F * a, 1.0F, 0.9F));
		}
	}

	/////////////////////////////////////////////
// we should soon be loading these files from the same place as the svx as well as this general directory
	void LoadSymbols(FileAbstraction fasymbols)
	{
		TN.emitMessage("Loading symbols " + fasymbols.getName());

		// do the tunnel loading thing
		TunnelLoader symbtunnelloader = new TunnelLoader(null, this);
		try
		{
			//symbolsdisplay.vgsymbols.tundirectory = fasymbols;  // the directory of symbols (trying to inline the function below)
			FileAbstraction.FileDirectoryRecurse(symbolsdisplay.vgsymbols, fasymbols);
			symbtunnelloader.LoadFilesRecurse(symbolsdisplay.vgsymbols);    // type OneTunnel

			// load up sketches
			for (OneSketch tsketch : symbolsdisplay.vgsymbols.tsketches)
				symbtunnelloader.LoadSketchFile(symbolsdisplay.vgsymbols, tsketch, false);
		}
		catch (IOException ie)
		{
			TN.emitWarning(ie.toString());
			ie.printStackTrace();
		}
		catch (NullPointerException e)
		{
			TN.emitWarning(e.toString());
			e.printStackTrace();
		};
	}

	/////////////////////////////////////////////
	SubsetAttrStyle GetSubsetSelection(String lstylename) // dead func
	{
		for (SubsetAttrStyle sas : subsetattrstylesmap.values())
		{
			if (lstylename.equals(sas.stylename))
				return sas; 
		}
		TN.emitWarning("Not found subsetstylename " + lstylename); 
		return null; 
	}

	/////////////////////////////////////////////
	// this gets called on opening, and whenever a set of sketches which contains some fontcolours gets loaded
	void UpdateSymbols(boolean bfirsttime)
	{
		assert bsubsetattributesneedupdating;
		// update the underlying symbols
		for (OneSketch tsketch : symbolsdisplay.vgsymbols.tsketches)
		{
			assert tsketch.bsketchfileloaded;
			tsketch.MakeAutoAreas();
		}

		// fill in all the attributes
		for (SubsetAttrStyle sas : subsetattrstylesmap.values())
			sas.FillAllMissingAttributes();

		// push the newly loaded stuff into the panels
		SetupSymbolStyleAttr();
		pthstyleareasigtab.UpdateAreaSignals(areasignames, nareasignames);

		// extract out and sort
		List<SubsetAttrStyle> lsaslist = new ArrayList<SubsetAttrStyle>();
		for (SubsetAttrStyle lsas : subsetattrstylesmap.values())
		{
			if (lsas.bselectable)
		        lsaslist.add(lsas);
		}
		Collections.sort(lsaslist);

		int iprevselindex = sketchdisplay.subsetpanel.jcbsubsetstyles.getSelectedIndex();
  		sketchdisplay.subsetpanel.jcbsubsetstyles.removeAllItems();
		for (SubsetAttrStyle lsas : lsaslist)
			sketchdisplay.subsetpanel.jcbsubsetstyles.addItem(lsas);

		bsubsetattributesneedupdating = false;

		if ((iprevselindex != -1) && (iprevselindex < sketchdisplay.subsetpanel.jcbsubsetstyles.getItemCount()))
			sketchdisplay.subsetpanel.jcbsubsetstyles.setSelectedIndex(iprevselindex);
	}
};


