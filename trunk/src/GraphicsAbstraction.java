////////////////////////////////////////////////////////////////////////////////
// TunnelX -- Cave Drawing Program
// Copyright (C) 2006  Martin Green.
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

import java.util.Iterator;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.Image; 

//
//
// GraphicsAbstraction
//
//

/////////////////////////////////////////////
// This will eventually be the base class for all graphics interacton
public class GraphicsAbstraction
{
	Graphics2D g2d;
	Rectangle2D printrect = null; // used for thinning out the frames

	private Shape mainclip = null;
	private AffineTransform preframetrans = null;
	private Shape frameclip = null; // active if not null

	GraphicsAbstraction(Graphics2D pg2d)
	{
		g2d = pg2d;
		mainclip = g2d.getClip();
	}

	private void setColor(Color c)
	{
		g2d.setColor(c);
	}
	private void setStroke(BasicStroke bs)
	{
		g2d.setStroke(bs);
	}
	private void draw(Shape shape)
	{
		assert shape != null;
        try
        {
		  g2d.draw(shape);
        }
        catch (java.lang.ArithmeticException e)
        { TN.emitMessage("ArithmeticException in javadraw library" + e.toString()); }
        catch (java.lang.InternalError e)
        { TN.emitMessage("InternalError in javadraw library" + e.toString()); }
        catch (java.lang.NullPointerException e)
        { TN.emitMessage("NullPointerException in javadraw library" + e.toString()); }

	}
	private void fill(Shape shape)
	{
		g2d.fill(shape);
	}
	private void setFont(Font font)
	{
		g2d.setFont(font);
	}
	private void drawString(String s, Float x, Float y)
	{
		g2d.drawString(s, x, y);
	}
	private Shape getClip()
	{
		return g2d.getClip();
	}
	private void setClip(Shape shape)
	{
		//System.out.println("setclip " + shape.getBounds2D(getTransform())); 
		g2d.setClip(shape);
	}
	private void clip(Shape shape)
	{
		g2d.clip(shape);
	}
	private AffineTransform getTransform()
	{
		return g2d.getTransform();
	}
	private void setTransform(AffineTransform at)
	{
		g2d.setTransform(at);
		//mainclip = g2d.getClip();
	}

	void transform(AffineTransform at)
	{
		g2d.transform(at);
		//mainclip = g2d.getClip();
	}
	void SetMainClip()
	{
		mainclip = g2d.getClip();
	}

	Boolean hit(Rectangle rect, Shape shape, Boolean bool)
	{
		return g2d.hit(rect, shape, bool);
	}

	//Algorithms to handle clipping
	void startSymbolClip(OneSArea osa)
	{
		clip(osa.aarea); //Intersects the current clip with gparea
	}
	void startSymbolClip(ConnectiveComponentAreas cca)
	{
		clip(cca.saarea); //Intersects the current clip with gparea
	}
	void startAccPolyClip(Shape shap)
	{
		clip(shap);
	}
	void endClip()
	{
		setClip(frameclip != null ? frameclip : mainclip);
	}

	void startFrame(OneSArea osa, AffineTransform at)
	{
System.out.println("startframe " + g2d.getClipBounds()); 
		assert frameclip == null;
		if (osa != null)
			clip(osa.gparea);
		assert preframetrans == null;
		preframetrans = getTransform();
		transform(at);
		frameclip = getClip();  // this must be got after the transform
//		assert frameclip != null;
	}
	// mainclip = g2d.getClip();

	void endFrame()
	{
//		assert frameclip != null;
		setTransform(preframetrans);
		preframetrans = null;
		frameclip = null;
		setClip(mainclip);
System.out.println("endframe " + g2d.getClipBounds() + "  " + mainclip); 
	}

	void drawString(String s, LabelFontAttr lfa, float x, float y)
	{
		drawString(s, lfa, x, y, null); 
	}
	
	void drawString(String s, LabelFontAttr lfa, float x, float y, Color color)
	{
		setFont(lfa.fontlab);
		setColor(color != null ? color : lfa.labelcolour);
		drawString(s, x, y);
	}

	void drawlabel(PathLabelDecode pld, float x, float y, Color labelcolour)
	{
		// draw the box outline of the whole label
		if (labelcolour == null)
			labelcolour = pld.labfontattr.labelcolour;

		if ((pld.bboxpresent) && (pld.rectdef != null))
		{
			setColor(labelcolour);
			assert pld.labfontattr.labelstroke != null;
			setStroke(pld.labfontattr.labelstroke);
			draw(pld.rectdef);
		}

		//draw the text
		for (PathLabelElement ple : pld.vdrawlablns)
		{
			// the black and white rectangles
			if (ple.text.equals("%blackrect%"))
			{
				setColor(labelcolour);
				g2d.fill(ple.textshape);
			}
			// what makes this complicated is that a straight outline exceeds the boundary of the rectangle, so must be trimmed.  The line is then halfwidth
			else if (ple.text.equals("%whiterect%"))
			{
				setColor(labelcolour);
				assert pld.labfontattr.labelstroke != null;
				setStroke(pld.labfontattr.labelstroke);
				startAccPolyClip(ple.textshape);
				draw(ple.textshape);
				endClip();
			}

			// we could fill translucent for the writing to show up better
			// or put this into the rectef above, unioning the different
			// rectangles that are slightly expanded
			else
			{
				//setColor(Color.red);
				//g2d.fill(ple.textrect);

				if (ple.bfontmagnifyset)
				{
					Font dfont = pld.labfontattr.fontlab.deriveFont(ple.fontmagnify * pld.labfontattr.fontlab.getSize()); 
System.out.println("font sizes " + pld.labfontattr.fontlab.getSize() + " " + dfont.getSize() + " on text " + ple.text); 
					setFont(dfont);
				}
				else
					setFont(pld.labfontattr.fontlab);
				setColor(labelcolour);
				drawString(ple.text, (float)(ple.textrect.getX() + ple.ftextjustify * (pld.drawlabxwid - ple.textwidth)), (float)ple.textrect.getY() + (float)ple.textrect.getHeight() - pld.fmdescent);
			}
		}

		//Draw arrow
		if (pld.barrowpresent)
		{
			for (int i = 0; i < pld.arrowdef.length; i++)
			{
				setStroke(pld.labfontattr.labelstroke);
				setColor(labelcolour);
				if (pld.arrowdef[i] != null)
					draw(pld.arrowdef[i]);
			}
		}
	}

	void drawPath(OnePath op, LineStyleAttr linestyleattr)
		{ drawPath(op, linestyleattr, null);  }
	void drawPath(OnePath op, LineStyleAttr linestyleattr, Color color)
	{
		//assert linestyleattr.strokecolour != null;
		assert op != null;

		// set the colour
		setColor(color != null ? color : linestyleattr.strokecolour);
		if (op.linestyle == SketchLineStyle.SLS_FILLED)
		{
			fill(op.gp);
			return;
		}

		// set the stroke
		assert linestyleattr.linestroke != null;
		setStroke(linestyleattr.linestroke);

		// special spiked type things
		if (linestyleattr.spikeheight != 0.0F)
		{
			assert ((op.linestyle == SketchLineStyle.SLS_PITCHBOUND) || (op.linestyle == SketchLineStyle.SLS_CEILINGBOUND));
			drawDottedPath(op, linestyleattr.strokewidth / 2, linestyleattr.gapleng, linestyleattr.spikegap, linestyleattr.spikeheight);
		}

		// other visible strokes
		else
			draw(op.gp);
	}
	void drawShape(Shape shape, LineStyleAttr linestyleattr)
		{ drawShape(shape, linestyleattr, null);  }

	void drawShape(Shape shape, LineStyleAttr linestyleattr, Color color) // Just used for odd things like dotted cut out rectangles
	{
		// set the colour
		setColor(color != null ? color : linestyleattr.strokecolour);

		// set the stroke
        if (linestyleattr != null)
        {
            assert linestyleattr.linestroke != null;
            setStroke(linestyleattr.linestroke);
        }

		//Draw the shape
		draw(shape);
	}

	/////////////////////////////////////////////
	boolean drawDottedPath(OnePath op, float flatness, float gapleng, float spikegap, float spikeheight)
	{
		float[] coords = new float[6];
		float[] pco = new float[op.nlines * 6 + 2];


		// maybe we will do this without flattening paths in the future.
		FlatteningPathIterator fpi = new FlatteningPathIterator(op.gp.getPathIterator(null), flatness);
		if (fpi.currentSegment(coords) != PathIterator.SEG_MOVETO)
			TN.emitProgError("move to not first");

		// put in the moveto.
		float lx = coords[0];
		float ly = coords[1];
		// (gapleng == 0.0F) means pitch bound.
		int scanmode = (gapleng == 0.0F ? 1 : 0); // 0 for blank, 1 for approaching a spike, 2 for leaving a spike.
		float dotleng = spikegap - gapleng;
		if (dotleng <= 0.0)
            return TN.emitWarning("Dotleng "+dotleng+ "  spikegap="+spikegap+"  gapleng="+gapleng); 
		float scanlen = dotleng / 2;

		fpi.next();
		while (!fpi.isDone())
		{
			int curvtype = fpi.currentSegment(coords);

			//if (curvtype == PathIterator.SEG_LINETO)
			if (curvtype != PathIterator.SEG_LINETO)
				TN.emitProgError("Flattened not lineto");

			// measure the distance to the coords.
			float vx = coords[0] - lx;
			float vy = coords[1] - ly;
			float dfco = (float)Math.sqrt(vx * vx + vy * vy);
			float lam = 0.0F;
			float dfcoR = dfco;
			float lxR = lx;
			float lyR = ly;
			boolean bCont = false;

			while ((scanlen <= dfcoR) && (lam != 1.0F) && (dfcoR != 0.0F))
			{
				// find the lam where this ends
				float lam1 = Math.min(1.0F, lam + scanlen / dfco);
				float lx1 = lx + vx * lam1;
				float ly1 = ly + vy * lam1;
				if (scanmode != 0)
				{
					draw(new Line2D.Float(lxR, lyR, lx1, ly1));
				}

				lxR = lx1;
				lyR = ly1;
				lam = lam1;
				dfcoR -= scanlen;

				// spike if necessary
				if (scanmode == 1)
				{
					// right hand spike.
					if (spikeheight != 0.0F)
						draw(new Line2D.Float(lxR, lyR, lxR - vy * spikeheight / dfco, lyR + vx * spikeheight / dfco));

					if (gapleng != 0.0F)
					{
						scanmode = 2;
						scanlen = spikegap / 2;
					}
					else 
						scanlen = spikegap;
				}
				else if (scanmode == 0)
				{
					scanlen = spikegap / 2;
					scanmode = 1;
				}
				else
				{
					scanlen = dotleng;
					scanmode = 0;
				}
			}

			if (scanmode != 0)
				draw(new Line2D.Float(lxR, lyR, coords[0], coords[1]));

			scanlen -= dfcoR;

			lx = coords[0];
			ly = coords[1];

			fpi.next();
		}
        return true; 
	}


	/////////////////////////////////////////////
	double revangle(int i)
	{
		if (i == 0)
			return 0.0; 
		int irs = 1; 
		int ir = 0; 
		while (i != 0)
		{
			ir = ir << 1; 
			if ((i & 1) != 0)
				ir++; 
			irs = irs << 1; 
			i = i >> 1; 
		}
		return (double)ir / irs; 
	}

	/////////////////////////////////////////////
	void drawHatchedArea(OneSArea osa, int isa)
	{
		if (osa.gparea == null)
			return;

System.out.println("revangle " + isa + ": " + revangle(isa)); 
		// make the hatch path.
		GeneralPath gphatch;
		{
			gphatch = new GeneralPath();

			// find the region we will make our parallel lines in.
			Rectangle2D r2d = osa.rboundsarea;
			double midx = r2d.getX() + r2d.getWidth() / 2;
			double midy = r2d.getY() + r2d.getHeight() / 2;
			double mrad = Math.sqrt(r2d.getWidth() * r2d.getWidth() + r2d.getHeight() * r2d.getHeight()) / 2;

			double mtheta = Math.PI * revangle(isa) + 0.12345F;
			double vx = Math.cos(mtheta);
			double vy = Math.sin(mtheta);

			double sp = SketchLineStyle.strokew * 5.0F;
			int gg = (int)(mrad / sp + 1.0F);
			for (int i = -gg; i <= gg; i++)
			{
				double scx = midx + vy * sp * i;
				double scy = midy - vx * sp * i;

				gphatch.moveTo((float)(scx - vx * mrad), (float)(scy - vy * mrad));
				gphatch.lineTo((float)(scx + vx * mrad), (float)(scy + vy * mrad));
			}
		}

		// we have the hatching path.  now draw it clipped.  Sybmol Clip is used as hatching works simialarly to symbols
		startSymbolClip(osa);
		drawShape(gphatch, (isa % 2) == 0 ? SketchLineStyle.linestylehatch1 : SketchLineStyle.linestylehatch2);
		endClip();
	}

	void fillArea(ConnectiveComponentAreas cca, Color color)
	{
		setColor(color);
		fill(cca.saarea);
	}

	void fillArea(OneSArea osa, Color color)
	{
		setColor(color);
		fill(osa.aarea);
	}

	void drawSymbol(OneSSymbol oss, LineStyleAttr linestyleattr)
	{
		if (oss.gpsymps == null)
			return;
		setColor(linestyleattr.strokecolour);
		if (oss.ssb.bFilledType)
			fill(oss.gpsymps);
		else
		{
			setStroke(linestyleattr.linestroke);
			draw(oss.gpsymps);
		}
	}

	/////////////////////////////////////////////
	void drawImage(Image img)
	{
		try
        {
            g2d.drawImage(img, null, null);
        }
		catch (NoClassDefFoundError e)
        {
            TN.emitWarning("Out of memory error while drawing image"); 
        }
	}
	
	/////////////////////////////////////////////
	// make gradient shading within an area
	void pwqFillArea(OneSArea osa)
	{
		assert osa.subsetattr != null;
		if (osa.subsetattr.areamaskcolour != null) //This shadow lightens the background, I think this should be combined with drawing the colour
			fillArea(osa, osa.subsetattr.areamaskcolour);

		if (osa.subsetattr.areacolour == null)
			return; 
		if (!SketchLineStyle.bDepthColours)
		{
			fillArea(osa, osa.subsetattr.areacolour);
			return; 
		}

		startSymbolClip(osa);

		int nx = 4; 
		int ny = 4; 
		double dx = osa.rboundsarea.getWidth() / nx; 
		double dy = osa.rboundsarea.getHeight() / ny; 
		for (int ix = 0; ix < nx; ix++)
		{
			double x0 = osa.rboundsarea.getX() + ix * dx; 
			double xc = x0 + dx / 2; 
			for (int iy = 0; iy < ny; iy++)
			{
				double y0 = osa.rboundsarea.getY() + iy * dy; 
				double yc = y0 + dy / 2; 
				float licollam = osa.GetAvgLocIcollam(xc, yc); 
				setColor(SketchLineStyle.GetColourFromCollam(licollam, true));
				fill(new Rectangle2D.Double(x0, y0, dx, dy));
			}
		}
		endClip(); 
	}
}


