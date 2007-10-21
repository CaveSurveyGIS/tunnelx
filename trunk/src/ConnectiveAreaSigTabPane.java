////////////////////////////////////////////////////////////////////////////////
// TunnelX -- Cave Drawing Program
// Copyright (C) 2004  Julian Todd.
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
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;



/////////////////////////////////////////////
class ConnectiveAreaSigTabPane extends JPanel
{
	JComboBox areasignals = new JComboBox();
	JButton jbcancel = new JButton("Cancel Area-signal");

	// we can choose to print just one view on one sheet of paper
	JButton tfrotatedegbutt = new JButton("Rotate:");
	JTextField tfrotatedeg = new JTextField();
	JButton tfscalebutt = new JButton("Scale:");
	JTextField tfscale = new JTextField();
 	JButton tfxtranscenbutt = new JButton("X-translate:");
	JTextField tfxtrans = new JTextField();
	JButton tfytranscenbutt = new JButton("Y-translate:");
	JTextField tfytrans = new JTextField();
	JButton tfsketchcopybutt = new JButton("Sketch:");
	JTextField tfsketch = new JTextField();

	JButton tfsubstylecopybutt = new JButton("Style:");
	JTextField tfsubstyle = new JTextField();
	SketchLineStyle sketchlinestyle;

	JButton tfzsetrelativebutt = new JButton("Z-Relative");
	JTextField tfzsetrelative = new JTextField();

	JButton tfsubmappingcopybutt = new JButton("Sub-mapping copy");
	JButton tfsubmappingpastebutt = new JButton("paste");
	JTextArea tfsubmapping = new JTextArea();

	// it might be necessary to back-up the initial value as well, so we wind up cycling through three values
	String saverotdeg = "0.0";
	String savescale = "1000.0";
	String savextrans = "0.0";
	String saveytrans = "0.0";
	String savesketch = "";
	String savesubstyle = "";
	String savesubmapping = "";

	String copiedsubmapping = "";

	/////////////////////////////////////////////
	void SketchCopyButt()
	{
		OneSketch asketch = sketchlinestyle.sketchdisplay.mainbox.tunnelfilelist.GetSelectedSketchLoad(); 
		String st = ""; 
		if (asketch != null)
		{
			OneSketch tsketch = sketchlinestyle.sketchdisplay.sketchgraphicspanel.tsketch;
			OneTunnel atunnel = asketch.sketchtunnel;
			st = asketch.sketchfile.getSketchName(); 
			while (atunnel != tsketch.sketchtunnel)
			{
				st = atunnel.name + "/" + st; 
				atunnel = atunnel.uptunnel; 
				if (atunnel == null)
				{
					TN.emitWarning("selected frame sketch must be in tree"); // so we can make this relative map to it
					st = "";
					break;  
				}
			}		
		}
		if (st.equals("") || (tfsketch.getText().equals(st) && !savesketch.equals("")))
			st = savesketch;
		else if (savesketch.equals(""))
			savesketch = st;
		tfsketch.setText(st);
		sketchlinestyle.GoSetParametersCurrPath();
	}

	/////////////////////////////////////////////
	void CopyBackgroundSketchTransform(String st, AffineTransform lat, Vec3 lsketchLocOffset)
	{
		AffineTransform at = (lat != null ? new AffineTransform(lat) : new AffineTransform());
		tfsketch.setText(st);

System.out.println("atatat " + at.toString());
		AffineTransform nontrat = new AffineTransform(at.getScaleX(), at.getShearY(), at.getShearX(), at.getScaleY(), 0.0, 0.0);
		double x0 = at.getScaleX();
		double y0 = at.getShearY();

		double x1 = at.getShearX();
		double y1 = at.getScaleY();

		double scale0 = Math.sqrt(x0 * x0 + y0 * y0);
		double scale1 = Math.sqrt(x1 * x1 + y1 * y1);

		//System.out.println("scsc " + scale0 + "  " + scale1);

		double rot0 = Vec3.DegArg(x0, y0);
		double rot1 = Vec3.DegArg(x1, y1);

		//System.out.println("rtrt " + rot0 + "  " + rot1);

		tfxtrans.setText(String.valueOf((float)((at.getTranslateX() + lsketchLocOffset.x) / TN.CENTRELINE_MAGNIFICATION)));
		tfytrans.setText(String.valueOf((float)((at.getTranslateY() - lsketchLocOffset.y) / TN.CENTRELINE_MAGNIFICATION)));

		tfscale.setText(scale0 != 0.0 ? String.valueOf((float)(1.0 / scale0)) : "0.0");
		tfrotatedeg.setText(String.valueOf(-(float)rot0));

		// need to undo the following transforms
		/*pframesketchtrans = new AffineTransform();
		pframesketchtrans.translate(-lsketchLocOffset.x * TN.CENTRELINE_MAGNIFICATION, +lsketchLocOffset.y * TN.CENTRELINE_MAGNIFICATION);
		pframesketchtrans.translate(sfxtrans * lrealpaperscale * TN.CENTRELINE_MAGNIFICATION, sfytrans * lrealpaperscale * TN.CENTRELINE_MAGNIFICATION);
		if (sfscaledown != 0.0)
			pframesketchtrans.scale(lrealpaperscale / sfscaledown, lrealpaperscale / sfscaledown);
		if (sfrotatedeg != 0.0)
			pframesketchtrans.rotate(-Math.toRadians(sfrotatedeg));*/

		sketchlinestyle.GoSetParametersCurrPath();
	}

	/////////////////////////////////////////////
	void StyleCopyButt()
	{
		SubsetAttrStyle sascurrent = sketchlinestyle.sketchdisplay.subsetpanel.sascurrent;
		if ((sascurrent != null) && tfsubstyle.getText().equals(savesubstyle))
			tfsubstyle.setText(sascurrent.stylename);
		else
			tfsubstyle.setText(savesubstyle);
		sketchlinestyle.GoSetParametersCurrPath();
	}

	/////////////////////////////////////////////
	void StyleMappingCopyButt(boolean bcopy)
	{
		if (bcopy)
		{
			savesubmapping = tfsubmapping.getText();
			tfsubmappingpastebutt.setToolTipText(savesubmapping);
		}
		else
			tfsubmapping.setText(savesubmapping);
	}

	/////////////////////////////////////////////
	void TransCenButt(int typ)
	{
		// find the area which this line corresponds to.  (have to search the areas to find it).
		OnePath op = sketchlinestyle.sketchdisplay.sketchgraphicspanel.currgenpath;
		if ((op == null) || (op.plabedl == null) || (op.plabedl.sketchframedef == null))
			return;
		OneSArea osa = (op.karight != null ? op.karight : op.kaleft);
		if ((op.plabedl.sketchframedef.pframesketch == null) || (osa == null))
		{
			TN.emitWarning("Need to make areas in this sketch first for this button to work");
			return;
		}
		String sval = op.plabedl.sketchframedef.TransCenButtF(typ, osa, sketchlinestyle.sketchdisplay.sketchgraphicspanel.tsketch.realpaperscale, sketchlinestyle.sketchdisplay.sketchgraphicspanel.tsketch.sketchLocOffset);

		if (typ == 0)
		{
			if (sval.equals("") || !tfscale.getText().equals(savescale))
				sval = savescale;
			tfscale.setText(sval);
		}
		else if (typ == 1)
		{
			if (sval.equals("") || !tfxtrans.getText().equals(savextrans))
				sval = savextrans;
			tfxtrans.setText(sval);
		}
		else
		{
			if (sval.equals("") || !tfytrans.getText().equals(saveytrans))
				sval = saveytrans;
			tfytrans.setText(sval);
		}
		sketchlinestyle.GoSetParametersCurrPath();
	}


	/////////////////////////////////////////////
	ConnectiveAreaSigTabPane(SketchLineStyle lsketchlinestyle)
	{
		super(new BorderLayout());
		sketchlinestyle = lsketchlinestyle;

		JPanel ntop = new JPanel(new BorderLayout());
		ntop.add(new JLabel("Area Signals", JLabel.CENTER), BorderLayout.NORTH);

		JPanel pie = new JPanel();
		pie.add(areasignals);
		pie.add(jbcancel);
		ntop.add(pie, BorderLayout.CENTER);

		add(ntop, BorderLayout.NORTH);

		JPanel pimpfields = new JPanel(new GridLayout(0, 2));
		pimpfields.add(tfrotatedegbutt);
		pimpfields.add(tfrotatedeg);
		pimpfields.add(tfscalebutt);
		pimpfields.add(tfscale);
		pimpfields.add(tfxtranscenbutt);
		pimpfields.add(tfxtrans);
		pimpfields.add(tfytranscenbutt);
		pimpfields.add(tfytrans);
		pimpfields.add(tfsketchcopybutt);
		pimpfields.add(tfsketch);
		pimpfields.add(tfsubstylecopybutt);
		pimpfields.add(tfsubstyle);
		pimpfields.add(tfzsetrelativebutt);
		pimpfields.add(tfzsetrelative);

		pimpfields.add(tfsubmappingcopybutt);
		pimpfields.add(tfsubmappingpastebutt);

		add(pimpfields, BorderLayout.CENTER);

		JPanel psm = new JPanel(new BorderLayout());
		psm.add(tfsubmapping, BorderLayout.CENTER);
		add(psm, BorderLayout.SOUTH);

		tfrotatedegbutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { tfrotatedeg.setText(saverotdeg);  sketchlinestyle.GoSetParametersCurrPath(); } } );
		tfscalebutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { TransCenButt(0); } } );
		tfxtranscenbutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { TransCenButt(1); } } );
		tfytranscenbutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { TransCenButt(2); } } );
		tfsketchcopybutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { SketchCopyButt(); } } );
		tfsubstylecopybutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { StyleCopyButt(); } } );
		tfzsetrelativebutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { tfzsetrelative.setText("0.0");  sketchlinestyle.GoSetParametersCurrPath(); } } );
		tfsubmappingcopybutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { StyleMappingCopyButt(true); } } );
		tfsubmappingpastebutt.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent event)  { StyleMappingCopyButt(false); } } );
	}

	/////////////////////////////////////////////
	void UpdateAreaSignals(String[] areasignames, int nareasignames)
	{
		areasignals.removeAllItems();
		for (int i = 0; i < nareasignames; i++)
			areasignals.addItem(areasignames[i]);
	}

	/////////////////////////////////////////////
	void SetFrameSketchInfoText(OnePath op)
	{
		boolean bsketchframe = ((op != null) && (op.plabedl.barea_pres_signal == SketchLineStyle.ASE_SKETCHFRAME));
		boolean bnodeconnzrelative = ((op != null) && (op.plabedl.barea_pres_signal == SketchLineStyle.ASE_ZSETRELATIVE));

		if (bsketchframe)
		{
			if (op.plabedl.sketchframedef == null)
				op.plabedl.sketchframedef = new SketchFrameDef();

			tfscale.setText(Float.toString(op.plabedl.sketchframedef.sfscaledown));
			tfrotatedeg.setText(String.valueOf(op.plabedl.sketchframedef.sfrotatedeg));
			tfxtrans.setText(Float.toString(op.plabedl.sketchframedef.sfxtrans));
			tfytrans.setText(Float.toString(op.plabedl.sketchframedef.sfytrans));
			tfsketch.setText(op.plabedl.sketchframedef.sfsketch);
			tfsubstyle.setText(op.plabedl.sketchframedef.sfstyle);
		}
		else
		{
			if (!tfrotatedeg.getText().trim().equals(""))
				saverotdeg = tfrotatedeg.getText();
			tfrotatedeg.setText("");
			if (!tfscale.getText().trim().equals(""))
				savescale = tfscale.getText();
			tfscale.setText("");
			if (!tfxtrans.getText().trim().equals(""))
				savextrans = tfxtrans.getText();
			tfxtrans.setText("");
			if (!tfytrans.getText().trim().equals(""))
				saveytrans = tfytrans.getText();
			tfytrans.setText("");
			if (!tfsketch.getText().trim().equals(""))
				savesketch = tfsketch.getText();
			tfsketch.setText("");
			if (!tfsubstyle.getText().trim().equals(""))
				savesubstyle = tfsubstyle.getText();
			tfsubstyle.setText("");
		}

		if (bnodeconnzrelative || bsketchframe)
			tfzsetrelative.setText(String.valueOf(op.plabedl.nodeconnzsetrelative));
		else
			tfzsetrelative.setText("");

		tfscalebutt.setEnabled(bsketchframe);
		tfscale.setEditable(bsketchframe);
		tfrotatedegbutt.setEnabled(bsketchframe);
		tfrotatedeg.setEditable(bsketchframe);
		tfxtranscenbutt.setEnabled(bsketchframe);
		tfxtrans.setEditable(bsketchframe);
		tfytranscenbutt.setEnabled(bsketchframe);
		tfytrans.setEditable(bsketchframe);
		tfsketchcopybutt.setEnabled(bsketchframe);
		tfsketch.setEditable(bsketchframe);
		tfsubstylecopybutt.setEnabled(bsketchframe);
		tfsubstyle.setEditable(bsketchframe);

		tfzsetrelativebutt.setEnabled(bnodeconnzrelative || bsketchframe);
		tfzsetrelative.setEditable(bnodeconnzrelative || bsketchframe);
	}
};


