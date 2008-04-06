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

import java.awt.Graphics2D;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import java.io.IOException;
import java.lang.StringBuffer;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/////////////////////////////////////////////
class OneSketch
{
	// this must always be set
	FileAbstraction sketchfile = null;
	boolean bsketchfileloaded = false;
	OneTunnel sketchtunnel = null;

	// arrays of sketch components.
	String sketchsymbolname; // not null if it's a symbol type
	boolean bSymbolType = false; // tells us which functions are allowed.

	// this could keep an update of deletes, inserts, and changes in properties (a flag on the path)
	boolean bsketchfilechanged = false;

	// main sketch.
	List<OnePathNode> vnodes;
	List<OnePath> vpaths;   // this is saved out into XML
	OnePath opframebackgrounddrag = null;

	Vec3 sketchLocOffset; // sets it to zero by default
	double realpaperscale = TN.defaultrealpaperscale;
	Rectangle2D rbounds = null;

	boolean bZonnodesUpdated = false;
	boolean bSAreasUpdated = false;
	boolean bSymbolLayoutUpdated = false;

	SortedSet<OneSArea> vsareas;

	Set<String> sallsubsets;

	List<String> backgroundimgnamearr;
	List<AffineTransform> backgimgtransarr;
	int ibackgroundimgnamearrsel = -1;

	// this gets the clockwise auto-area.
	OneSArea cliparea = null;
	SketchSymbolAreas sksya;  // this is a vector of ConnectiveComponents

	// range and restrictions in the display.
	boolean bRestrictSubsetCode = false;

	float zaltlo;
	float zalthi;

	SubsetAttrStyle sksascurrent = null;
	Map<String, String> submappingcurrent = new TreeMap<String, String>();  // cache this as well so we can tell when it changes (not well organized)

	boolean binpaintWquality = false;  // used to avoid frame drawing recursion
	boolean bWallwhiteoutlines = true;  // some flag that ought to be passed in
	static Color colframebackgroundshow = new Color(0.4F, 0.7F, 0.4F, 0.2F);
	static Color colframebackgroundimageshow = new Color(0.7F, 0.4F, 0.7F, 0.2F);

	/////////////////////////////////////////////
	OneSketch(FileAbstraction lsketchfile, OneTunnel lsketchtunnel)
	{
		sketchfile = lsketchfile;
		bsketchfileloaded = false;
		sketchtunnel = lsketchtunnel;
	}

	/////////////////////////////////////////////
	void UpdateSomething(int scchangetyp, boolean bforce)
	{
		if (((scchangetyp == SketchGraphics.SC_UPDATE_ZNODES) || (scchangetyp == SketchGraphics.SC_UPDATE_ALL) || (scchangetyp == SketchGraphics.SC_UPDATE_ALL_BUT_SYMBOLS))&& (bforce || !bZonnodesUpdated))
		{
			ProximityDerivation pd = new ProximityDerivation(this);
			pd.SetZaltsFromCNodesByInverseSquareWeight(this); // passed in for the zaltlo/hi values
			bZonnodesUpdated = true;
		}
		if (((scchangetyp == SketchGraphics.SC_UPDATE_AREAS) || (scchangetyp == SketchGraphics.SC_UPDATE_ALL) || (scchangetyp == SketchGraphics.SC_UPDATE_ALL_BUT_SYMBOLS))&& (bforce || !bSAreasUpdated))
		{
			MakeAutoAreas();  // once it is on always this will be unnecessary.
			assert OnePathNode.CheckAllPathCounts(vnodes, vpaths);
			// used to be part of the Update symbols areas, but brought here
			// so we have a full set of paths associated to each area available
			// for use to pushing into subsets.
			MakeConnectiveComponentsT();
			for (OneSArea osa : vsareas)
				 osa.SetSubsetAttrsA(true, sksascurrent);
			bSAreasUpdated = true;
		}
		if (((scchangetyp == SketchGraphics.SC_UPDATE_SYMBOLS) || (scchangetyp == SketchGraphics.SC_UPDATE_ALL)) && (bforce || !bSymbolLayoutUpdated))
		{
			boolean ballsymbolslayed = MakeSymbolLayout(null, null);
			assert ballsymbolslayed;
			bSymbolLayoutUpdated = true;
		}
	}

	/////////////////////////////////////////////
	void SetupSK()
	{
		assert !bsketchfileloaded;

		// main sketch.
		vnodes = new ArrayList<OnePathNode>();
		vpaths = new ArrayList<OnePath>();   // this is saved out into XML
		sketchLocOffset = new Vec3(0.0F, 0.0F, 0.0F); // sets it to zero by default
		vsareas = new TreeSet<OneSArea>();
		sallsubsets = new HashSet<String>();
		backgroundimgnamearr = new ArrayList<String>(); 
		backgimgtransarr = new ArrayList<AffineTransform>();
		sksya = new SketchSymbolAreas();  // this is a vector of ConnectiveComponents

		bsketchfileloaded = true; 
	}
	
	
	/////////////////////////////////////////////
	void ApplySplineChange()
	{
		for (OnePath op : vpaths)
		{
			if (OnePath.bHideSplines && op.bSplined)
				op.Spline(false, false);
			else if (!OnePath.bHideSplines && !op.bSplined && op.bWantSplined)
				op.Spline(true, false);
		}
	}




	/////////////////////////////////////////////
	// the complexity comes when the opfront is also in the list and must be suppressed.
	OnePathNode SelNode(OnePathNode opfront, boolean bopfrontvalid, Graphics2D g2D, Rectangle selrect, OnePathNode selpathnodecycle)
	{
		boolean bOvWrite = true;
		OnePathNode selnode = null;
		for (int i = 0; i <= vnodes.size(); i++)
		{
			OnePathNode pathnode = (i < vnodes.size() ? vnodes.get(i) : opfront);
			if ((pathnode != null) && (bopfrontvalid || (pathnode != opfront)) && (bOvWrite || (pathnode == selpathnodecycle)) && g2D.hit(selrect, pathnode.Getpnell(), false))
			{
				boolean lbOvWrite = bOvWrite;
				bOvWrite = (pathnode == selpathnodecycle);
				if (lbOvWrite)
					selnode = pathnode;
			}
		}
		return selnode;
	}


	/////////////////////////////////////////////
	OnePath SelPath(Graphics2D g2D, Rectangle selrect, OnePath prevselpath, List<OnePath> tsvpathsviz)
	{
		boolean bOvWrite = true;
		OnePath selpath = null;
		assert selrect != null;
		int isel = -1;
		for (int i = tsvpathsviz.size() - 1; i >= 0; i--)
		{
			OnePath path = tsvpathsviz.get(i); 
			assert path.gp != null;
			if ((bOvWrite || (path == prevselpath)) &&
				(g2D.hit(selrect, path.gp, true) ||
				 ((path.plabedl != null) && (path.plabedl.drawlab != null) && (path.plabedl.rectdef != null) && g2D.hit(selrect, path.plabedl.rectdef, false))))
			{
				boolean lbOvWrite = bOvWrite;
				bOvWrite = (path == prevselpath);
				if (lbOvWrite)
					selpath = path;
			}
		}
		return selpath;
	}


	/////////////////////////////////////////////
	OneSArea SelArea(Graphics2D g2D, Rectangle selrect, OneSArea prevselarea)
	{
		boolean bOvWrite = true;
		OneSArea selarea = null;
		int isel = -1;
		for (OneSArea oa : vsareas)
		{
			if ((bOvWrite || (oa == prevselarea)) && g2D.hit(selrect, oa.gparea, false))
			{
				boolean lbOvWrite = bOvWrite;
				bOvWrite = (oa == prevselarea);
				if (lbOvWrite)
					selarea = oa;
			}
		}
		return selarea;
	}

	/////////////////////////////////////////////
	int AddBackgroundImage(String lbackgroundimgname, AffineTransform lbackgimgtrans)
	{
		//System.out.println("Adding background " + lbackgroundimgname);
		assert backgimgtransarr.size() == backgimgtransarr.size();
		backgroundimgnamearr.add(lbackgroundimgname);
		backgimgtransarr.add(lbackgimgtrans);
		return backgimgtransarr.size() - 1;
	}

	/////////////////////////////////////////////
	int RemoveBackgroundImage(int libackgroundimgnamearrsel)
	{
		// not well designed function here.  called only from sketchbackgroundpanel.  the above one is only there because it's from XMLparse
		assert backgimgtransarr.size() == backgimgtransarr.size();
		backgroundimgnamearr.remove(libackgroundimgnamearrsel);
		backgimgtransarr.remove(libackgroundimgnamearrsel);
		return backgimgtransarr.size();
	}

	/////////////////////////////////////////////
	OnePath GetAxisPath()
	{
		for (OnePath op : vpaths)
		{
			if (op.linestyle == SketchLineStyle.SLS_CENTRELINE)
				return op;
		}
		return null;
	}


	/////////////////////////////////////////////
	void MakeConnectiveComponentsT()
	{
		// use new symbol layout engine
		sksya.MakeSSA(vpaths, vsareas);
	}

	/////////////////////////////////////////////
	boolean MakeSymbolLayout(GraphicsAbstraction ga, Rectangle windowrect)
	{
		// go through the symbols and find their positions and take them out.
		boolean bres = true;
		for (MutualComponentArea mca : sksya.vconncommutual)
		{
			if ((windowrect == null) || mca.hit(ga, windowrect))
				mca.LayoutMutualSymbols(); // all symbols in this batch
			else
			{
				//TN.emitMessage("skipping mutualcomponentarea");
				bres = false;
			}
		}
		return bres;
	}




	/////////////////////////////////////////////
	void AddArea(OnePath lop, boolean lbFore, List<OneSArea> vsareastakeout)
	{
		OneSArea osa = new OneSArea(lop, lbFore);
		if (osa.gparea == null) // no area (just a tree)
		{
			vsareastakeout.add(osa);
			osa.iareapressig = SketchLineStyle.ASE_NOAREA; 
			return;  // no linking created
		}
        // iareapressig gets picked up by the iteration around the contour the paths which make up this area

		// the clockwise path is the one bounding the outside.
		// it will say how many distinct pieces there are.

		int aread = OneSArea.FindOrientationReliable(osa.gparea);

		// can't determin orientation (should set the karight to null)
		if (aread != 1) // good areas are always clockwise
		{
			if (aread == -1)
			{
				if (bSymbolType && (cliparea != null))
					TN.emitWarning("More than one outerarea for cliparea in symbol " + sketchsymbolname);
				cliparea = osa; // the outer area thing if not a
			}
			osa.iareapressig = SketchLineStyle.ASE_OUTERAREA;
			vsareastakeout.add(osa);
			return;
		}

		// take out the areas that have been knocked out by area_signals
		if (osa.iareapressig == SketchLineStyle.ASE_KILLAREA) // rock/tree type (not pitchhole)
		{
			vsareastakeout.add(osa);
			return;
		}

		vsareas.add(osa);
	}


	/////////////////////////////////////////////
	class opcenscomp implements Comparator<OnePath>
	{
		public int compare(OnePath op1, OnePath op2)
		{
			float zalt1 = Math.max(op1.pnstart.zalt, op1.pnend.zalt);
			float zalt2 = Math.max(op2.pnstart.zalt, op2.pnend.zalt);
			return (int)Math.signum(zalt1 - zalt2);
		}
	}

	/////////////////////////////////////////////
	void AttachRemainingCentrelines()
	{
		List<OnePath> opcens = new ArrayList<OnePath>();
		for (OnePath op : vpaths)
		{
			if ((op.linestyle == SketchLineStyle.SLS_CENTRELINE) && (op.karight == null) && (op.pnstart != null) && (op.pnend != null))
				opcens.add(op);
		}

		// get the order right and zip it up with the areas
		Collections.sort(opcens, new opcenscomp());
		int iopcens = 0;
		OneSArea osaprev = null;
		for (OneSArea osa : vsareas)
		{
			while (iopcens < opcens.size())
			{
				OnePath op = opcens.get(iopcens);
				float pzalt = Math.max(op.pnstart.zalt, op.pnend.zalt);
				if (pzalt > osa.zalt)
					break;
				if (osaprev != null)  // centrelines below the lowest area aren't associated with any of them, so get drawn first.
					osaprev.SetCentrelineThisArea(op);
				iopcens++;
			}
			osaprev = osa;
		}
		while (iopcens < opcens.size())  // final piece above the last area
		{
			OnePath op = opcens.get(iopcens);
			if (osaprev != null)
				osaprev.SetCentrelineThisArea(op);
			iopcens++;
		}
	}


	/////////////////////////////////////////////
	// fills in the opforeright values etc.
	// works selectively on a subset of vnodes.
	void MakeAutoAreas()
	{
		assert bsketchfileloaded;

		// set values to null.  esp the area links.
		for (OnePath op : vpaths)
		{
			op.karight = null;
			op.kaleft = null;
		}
		assert OnePathNode.CheckAllPathCounts(vnodes, vpaths);

		// build the main list which we keep in order for rendering
		vsareas.clear();
		cliparea = null;

		// now collate the areas.
		List<OneSArea> vsareastakeout = new ArrayList<OneSArea>();
		for (OnePath op : vpaths)
		{
			if (op.AreaBoundingType())
			{
				if (op.karight == null)
					AddArea(op, true, vsareastakeout); // this constructer makes all the links too.
				if (op.kaleft == null)
					AddArea(op, false, vsareastakeout); // this constructer makes all the links too.
			}
		}

		// Now clear out the links in the altareas
		for (OneSArea osa : vsareastakeout)
			osa.SetkapointersClear();

		if (vsareas.isEmpty())
			return;

		// make the range set of the areas
		// this is all to do with setting the zaltlam variable
		double zaaltlo = vsareas.first().zalt;
		double zaalthi = vsareas.last().zalt;
		assert zaaltlo <= zaalthi;

		double zaaltdiff = zaalthi - zaaltlo;
		if (zaaltdiff == 0.0)
			zaaltdiff = 1.0;
		for (OneSArea osa : vsareas)
		{
			//float zaltlam = (osa.zalt - zaaltlo) / zaaltdiff;

			// spread out a bit.
			//zaltlam = (zaltlam + (float)i / Math.max(1, vsareas.size() - 1)) / 2.0F;

			// set the shade for the filling in.
			osa.zaltcol = null;
		}

		if (!bSymbolType)
			AttachRemainingCentrelines();
	}



	/////////////////////////////////////////////
	int TAddPath(OnePath path, OneTunnel vgsymbols)
	{
		assert (path.apforeright == null) && (path.aptailleft == null);

		if (path.pnstart.pathcount == 0)
		{
			assert !vnodes.contains(path.pnstart);
			path.pnstart.SetNodeCloseBefore(vnodes, vnodes.size());
			vnodes.add(path.pnstart);
		}
		path.pnstart.InsertOnNode(path, false);

		if (path.pnend.pathcount == 0)
		{
			assert !vnodes.contains(path.pnend);
			path.pnend.SetNodeCloseBefore(vnodes, vnodes.size());
			vnodes.add(path.pnend);
		}
		path.pnend.InsertOnNode(path, true);

		vpaths.add(path);
		assert path.pnstart.CheckPathCount();
		assert path.pnend.CheckPathCount();

		return vpaths.size() - 1;
	}


	/////////////////////////////////////////////
	static RefPathO trefpath = new RefPathO(); 
	boolean TRemovePath(OnePath op)
	{
		// remove any areas automatically
		if (op.AreaBoundingType())
		{
			if (op.kaleft != null)
			{
				// can be falsified if there's been a change from a wall to a connective type
				//assert vsareas.contains(op.kaleft);
				vsareas.remove(op.kaleft);
				op.kaleft.SetkapointersClear();
			}
			if (op.karight != null)
			{
				//assert vsareas.contains(op.karight);
				vsareas.remove(op.karight);
				op.karight.SetkapointersClear();
			}
		}
		else if ((op.linestyle == SketchLineStyle.SLS_CONNECTIVE) && (op.pthcca != null))
		{
System.out.println("removingPathfrom CCA"); 
			// assert op.pthcca.vconnpaths.contains(op); // may have already been removed
			op.pthcca.vconnpaths.remove(op); 
		}

		trefpath.op = op; 
		trefpath.bFore = false; 
		if (op.pnstart.RemoveOnNode(trefpath))
			vnodes.remove(op.pnstart);
		trefpath.bFore = true;
		if (op.pnend.RemoveOnNode(trefpath))
			vnodes.remove(op.pnend);

		assert (op.pnstart.pathcount == 0) || op.pnstart.CheckPathCount();
		assert (op.pnend.pathcount == 0) || op.pnend.CheckPathCount();

		if (opframebackgrounddrag == op)
			opframebackgrounddrag = null;

		return vpaths.remove(op);
	}



	/////////////////////////////////////////////
	Rectangle2D getBounds(boolean bForce, boolean bOfSubset)
	{
		if (!bForce && (rbounds != null) && !bOfSubset)
			return rbounds;

		Rectangle2D.Float lrbounds = new Rectangle2D.Float();
		boolean bFirst = true;
		for (OnePath op : vpaths)
		{
			if (!bOfSubset || !bRestrictSubsetCode || op.bpathvisiblesubset)
			{
				if (bFirst)
				{
					lrbounds.setRect(op.getBounds(null));
                    bFirst = false;
			    }
				else
					lrbounds.add(op.getBounds(null));
			}
		}

		// cache the result
		if (!bOfSubset)
			rbounds = lrbounds;

		return lrbounds;
	}

	/////////////////////////////////////////////
	void WriteXML(LineOutputStream los) throws IOException
	{
		// we default set the sketch condition to unsplined for all edges.
		los.WriteLine(TNXML.xcomopen(0, TNXML.sSKETCH, TNXML.sSPLINED, "0", TNXML.sSKETCH_LOCOFFSETX, String.valueOf(sketchLocOffset.x), TNXML.sSKETCH_LOCOFFSETY, String.valueOf(sketchLocOffset.y), TNXML.sSKETCH_LOCOFFSETZ, String.valueOf(sketchLocOffset.z), TNXML.sSKETCH_REALPAPERSCALE, String.valueOf(realpaperscale)));

		for (int i = 0; i < backgroundimgnamearr.size(); i++)
		{
			// set the matrix (if it exists)
			AffineTransform backgimgtrans = backgimgtransarr.get(i);
			if (backgimgtrans != null)
			{
				double[] flatmat = new double[6];
				backgimgtrans.getMatrix(flatmat);
				los.WriteLine(TNXML.xcomopen(1, TNXML.sAFFINE_TRANSFORM, TNXML.sAFTR_M00, String.valueOf(flatmat[0]), TNXML.sAFTR_M10, String.valueOf(flatmat[1]), TNXML.sAFTR_M01, String.valueOf(flatmat[2]), TNXML.sAFTR_M11, String.valueOf(flatmat[3]), TNXML.sAFTR_M20, String.valueOf(flatmat[4]), TNXML.sAFTR_M21, String.valueOf(flatmat[5])));
			}

			// write the name of the file
			los.WriteLine(TNXML.xcom(2, TNXML.sSKETCH_BACK_IMG, TNXML.sSKETCH_BACK_IMG_FILE, backgroundimgnamearr.get(i), TNXML.sSKETCH_BACK_IMG_FILE_SELECTED, (i == ibackgroundimgnamearrsel ? "1" : "0")));

			if (backgimgtrans != null)
				los.WriteLine(TNXML.xcomclose(1, TNXML.sAFFINE_TRANSFORM));
		}

		// write out the paths.
// IIII this is where we number the path nodes
		for (OnePath op : vpaths)
		{
			int ind0 = vnodes.indexOf(op.pnstart);
			int ind1 = vnodes.indexOf(op.pnend);
			if ((ind0 != -1) && (ind1 != -1))
				op.WriteXMLpath(los, ind0, ind1, 1);
			else
				TN.emitProgError("Path_node missing end " + vpaths.indexOf(op));
		}

		los.WriteLine(TNXML.xcomclose(0, TNXML.sSKETCH));
	}







	/////////////////////////////////////////////
	void pwqWallOutlinesPath(GraphicsAbstraction ga, OnePath op)
	{
		if (op.ciHasrendered != 0)
			return;
		op.ciHasrendered = 1;
		if (bRestrictSubsetCode && op.bpathvisiblesubset)
			return;
		if ((op.linestyle == SketchLineStyle.SLS_INVISIBLE) || (op.linestyle == SketchLineStyle.SLS_CONNECTIVE))
			return;
		if (op.subsetattr.linestyleattrs[op.linestyle] == null)
			return;
		if (op.subsetattr.shadowlinestyleattrs[op.linestyle].linestroke == null)
			return;

		ga.drawPath(op, op.subsetattr.shadowlinestyleattrs[op.linestyle]);
	}

	/////////////////////////////////////////////
	void pwqWallOutlinesArea(GraphicsAbstraction ga, OneSArea osa)
	{
		for (RefPathO rpo : osa.refpathsub)
		{
			pwqWallOutlinesPath(ga, rpo.op);
			paintWqualityjoiningpaths(ga, rpo.ToNode(), true);
		}
	}

	/////////////////////////////////////////////
	void pwqPathsNonAreaNoLabels(GraphicsAbstraction ga, Rectangle2D abounds)
	{
		// check any paths if they are now done
		for (OnePath op : vpaths)
		{
			op.ciHasrendered = 0;

			if (op.linestyle == SketchLineStyle.SLS_CONNECTIVE)
			{
				op.pnstart.pathcountch++;
				op.pnend.pathcountch++;
				op.ciHasrendered = 2;
				continue;
			}

			// path belongs to an area
			if ((op.karight != null) || (op.kaleft != null))
				continue;

			// no shadows are painted on unarea types
			op.pnstart.pathcountch++;
			op.pnend.pathcountch++;
			op.ciHasrendered = 3;

			if ((abounds != null) && !op.gp.intersects(abounds))
				continue;

			// the rest of the drawing of this path with quality
			op.paintWquality(ga);
		}
	}

	/////////////////////////////////////////////
	static RefPathO srefpathconn = new RefPathO();
	void paintWqualityjoiningpaths(GraphicsAbstraction ga, OnePathNode opn, boolean bShadowpaths)
	{
		srefpathconn.ccopy(opn.ropconn);
		do
		{
			OnePath op = srefpathconn.op;
			if (bShadowpaths)
				pwqWallOutlinesPath(ga, op);
   			else if ((op.ciHasrendered != 3) && (op.pnstart.pathcountch == op.pnstart.pathcount) && (op.pnend.pathcountch == op.pnend.pathcount))
			{
				op.paintWquality(ga);
				op.ciHasrendered = 3;
			}
		}
		while (!srefpathconn.AdvanceRoundToNode(opn.ropconn));
	}

	/////////////////////////////////////////////
	void pwqPathsOnAreaNoLabels(GraphicsAbstraction ga, OneSArea osa, Rectangle2D abounds)
	{
		// got to do the associated centrelines first
		for (OnePath op : osa.connpathrootscen)
		{
			if (op.linestyle == SketchLineStyle.SLS_CENTRELINE)
			{
				assert (op.kaleft == osa) && (op.karight == osa);
				op.pnstart.pathcountch++;
				op.pnend.pathcountch++;
				op.paintWquality(ga);
				op.ciHasrendered = 3;

				// if any of these starts and ends trip over the count then we need to do their connections
				if (bWallwhiteoutlines)
				{
					// now embed drawing all the lines connecting to the two end-nodes
					if (op.pnstart.pathcountch == op.pnstart.pathcount)
						paintWqualityjoiningpaths(ga, op.pnstart, false);
					if (op.pnend.pathcountch == op.pnend.pathcount)
						paintWqualityjoiningpaths(ga, op.pnend, false);
				}
			}
		}

		// there are duplicates in the refpaths list, so we cannot inline this check
		for (RefPathO rpo : osa.refpaths)
			assert (rpo.op.ciHasrendered <= 1);

		// check any paths if they are now done
		for (RefPathO rpo : osa.refpaths)
		{
			OnePath op = rpo.op;

			assert ((op.karight == osa) || (op.kaleft == osa));
			if (op.ciHasrendered >= 2)
				continue;
			if (((op.karight != null) && !op.karight.bHasrendered) || ((op.kaleft != null) && !op.kaleft.bHasrendered))
				continue;
			op.ciHasrendered = 2;
			op.pnstart.pathcountch++;
			op.pnend.pathcountch++;
			assert op.pnstart.pathcountch <= op.pnstart.pathcount;
			assert op.pnend.pathcountch <= op.pnend.pathcount;
			if ((abounds != null) && !op.gp.intersects(abounds))
				continue;

			// the rest of the drawing of this path with quality
			if (bWallwhiteoutlines)
			{
				// now embed drawing all the lines connecting to the two end-nodes
				if (op.pnstart.pathcountch == op.pnstart.pathcount)
					paintWqualityjoiningpaths(ga, op.pnstart, false);
				if (op.pnend.pathcountch == op.pnend.pathcount)
					paintWqualityjoiningpaths(ga, op.pnend, false);
			}
			else
			{
				op.paintWquality(ga);
				op.ciHasrendered = 3;
			}
		}
	}



	/////////////////////////////////////////////
	void pwqSymbolsOnArea(GraphicsAbstraction ga, OneSArea osa)
	{
		//ga.setColor(SketchLineStyle.linestylecolprint);
		// check any symbols that are now done
		// (there will be only one last area to come through).

		// once all areas in the connective component have been rendered, the symbols get rendered.
		// in practice, this is equivalent to the connective component being rendered when the last area in its list gets rendered
		// after we render an area, the only changes could happen with the connective components that had that area
		for (ConnectiveComponentAreas mcca : osa.ccalist)
		{
			if (!bRestrictSubsetCode || mcca.bccavisiblesubset)
			{
				if (!mcca.bHasrendered)
				{
					boolean bHasr = false;  // basically does an and across values in this list -- might be better with a count
					for (OneSArea cosa : mcca.vconnareas)
					{
						if (!cosa.bHasrendered)
						{
							bHasr = true;
							break;
						}
					}
					if (!bHasr)
					{
						mcca.paintWsymbols(ga);
						mcca.bHasrendered = true;
					}
				}
			}
		}
	}

	/////////////////////////////////////////////
	void pwqFillArea(GraphicsAbstraction ga, OneSArea osa)
	{
		assert osa.subsetattr != null;
		if (osa.subsetattr.areamaskcolour != null) //This shadow lightens the background, I think this should be combined with drawing the colour
			ga.fillArea(osa, osa.subsetattr.areamaskcolour);

		if (osa.subsetattr.areacolour != null)
			ga.fillArea(osa, osa.zaltcol == null ? osa.subsetattr.areacolour : osa.zaltcol);
	}

	/////////////////////////////////////////////
	void SetSubsetAttrStyle(SubsetAttrStyle lsksascurrent, OneTunnel vgsymbols, SketchFrameDef sketchframedef)
	{
		sksascurrent = lsksascurrent;
		submappingcurrent.clear();
		submappingcurrent.putAll(sketchframedef.submapping);

		// this sets the values on the paths
		for (OnePath op : vpaths)
			op.SetSubsetAttrs(sksascurrent, vgsymbols, sketchframedef);

		// this goes again and gets the subsets into the areas from those on the paths
		for (OneSArea osa : vsareas)
			osa.SetSubsetAttrsA(true, sksascurrent);
	}

	/////////////////////////////////////////////
	public void paintWqualitySketch(GraphicsAbstraction ga, boolean bFullView, OneTunnel vgsymbols, SketchLineStyle sketchlinestyle)
	{
		assert OnePathNode.CheckAllPathCounts(vnodes, vpaths);
		binpaintWquality = true;

		// set up the hasrendered flags to begin with
		for (OneSArea osa : vsareas)
			osa.bHasrendered = false;
		for (ConnectiveComponentAreas cca : sksya.vconncom)
			cca.bHasrendered = false;
		for (OnePathNode opn : vnodes)
			opn.pathcountch = 0;  // count these up as we draw them

		// go through the paths and render those at the bottom here and aren't going to be got later
		pwqPathsNonAreaNoLabels(ga, null);

		// go through the areas and complete the paths as we tick them off.
		for (OneSArea osa : vsareas)
		{
			// draw the wall type strokes related to this area
			// this makes the white boundaries around the strokes !!!
			if (bWallwhiteoutlines)
				pwqWallOutlinesArea(ga, osa);

			// fill the area with a diffuse colour (only if it's a drawing kind)
			if (bFullView || !bRestrictSubsetCode || osa.bareavisiblesubset)  // setting just for previewing
			{
				if (osa.iareapressig == SketchLineStyle.ASE_KEEPAREA)
					pwqFillArea(ga, osa);

				// could have these sorted by group subset style, and remake it for these
				if ((osa.iareapressig == SketchLineStyle.ASE_SKETCHFRAME) && (osa.sketchframedefs != null) && (!bRestrictSubsetCode || osa.bareavisiblesubset))
				{
					// the frame sketch
					if ((ga.printrect != null) && !osa.gparea.intersects(ga.printrect))
					{
						TN.emitMessage("Skipping framed sketch: " + osa.sketchframedefs.get(0).sfsketch);
						continue; // jumps out of the if-s
					}

					// multiple cases are rare, so convenient to sort them on the fly for dynamicness.
					if (osa.sketchframedefs.size() >= 2)
						Collections.sort(osa.sketchframedefs);

					for (SketchFrameDef sketchframedef : osa.sketchframedefs)
					{
						// the plotting of an included image
						if (sketchframedef.pframeimage != null)
						{
							if ((sketchlinestyle.sketchdisplay.printingpanel.cbRenderingQuality.getSelectedIndex() == 1) || (sketchlinestyle.sketchdisplay.printingpanel.cbRenderingQuality.getSelectedIndex() == 3))
							{
								ga.startFrame((!sketchframedef.sfstyle.equals("notrim") ? osa : null), sketchframedef.pframesketchtrans);
								Image img = sketchframedef.pframeimage.GetImage(true);
								ga.drawImage(img);
								ga.endFrame();
							}
							else
								ga.fillArea(osa, colframebackgroundimageshow); // signifies that something's there (deliberately overpaints sketches when there's more than one, so it's visible)
							continue;
						}

						// the plotting of the sketch
						if (sketchframedef.pframesketch == null)
							continue;
						if (sketchframedef.pframesketch.binpaintWquality) // avoids recursion
							continue;
						if (!bFullView)
							ga.fillArea(osa, colframebackgroundshow); // signifies that something's there (deliberately overpaints sketches when there's more than one, so it's visible)

						//assert sketchframedef.pframesketch.sksascurrent != null;
						SubsetAttrStyle sksas = sketchlinestyle.subsetattrstylesmap.get(sketchframedef.sfstyle);
						if (sksas == null)
							sksas = sketchlinestyle.subsetattrstylesmap.get("default");
						assert (sksas != null);  // it has to at least be set to something; if it has been loaded in the background
						if ((sksas != null) && ((sksas != sketchframedef.pframesketch.sksascurrent) || !sketchframedef.pframesketch.submappingcurrent.equals(sketchframedef.submapping)))
						{
							int iProper = (sketchlinestyle.sketchdisplay.printingpanel.cbRenderingQuality.getSelectedIndex() == 3 ? SketchGraphics.SC_UPDATE_ALL : SketchGraphics.SC_UPDATE_ALL_BUT_SYMBOLS);
							TN.emitMessage("-- Resetting sketchstyle to " + sksas.stylename + " during rendering");
							sketchframedef.pframesketch.SetSubsetAttrStyle(sksas, vgsymbols, sketchframedef);
							SketchGraphics.SketchChangedStatic(SketchGraphics.SC_CHANGE_SAS, sketchframedef.pframesketch, null);
							assert (sksas == sketchframedef.pframesketch.sksascurrent);
							assert sketchframedef.pframesketch.submappingcurrent.equals(sketchframedef.submapping);

							// if iproper == SketchGraphics.SC_UPDATE_ALL (not SketchGraphics.SC_UPDATE_ALL_BUT_SYMBOLS)
							// then it could do it as through a window so that not the whole thing needs redoing.
							sketchframedef.pframesketch.UpdateSomething(iProper, false);
							SketchGraphics.SketchChangedStatic(iProper, sketchframedef.pframesketch, null);
						}

// copying in vital statistics into the ImageWarp object
/*ImageWarp backgroundimg = sketchlinestyle.sketchdisplay.sketchgraphicspanel.backgroundimg;
backgroundimg.osa = osa;
backgroundimg.pframesketchtrans = sketchframedef.pframesketchtrans;
backgroundimg.pframesketch = sketchframedef.pframesketch;
backgroundimg.vgsymbols = vgsymbols;*/
						ga.startFrame(osa, sketchframedef.pframesketchtrans);
						TN.emitMessage("Drawing the frame round: " + sketchframedef.sfsketch);
						sketchframedef.pframesketch.paintWqualitySketch(ga, true, vgsymbols, null);
						ga.endFrame();
					}
				}
			}
			assert !osa.bHasrendered;
			osa.bHasrendered = true;
			pwqSymbolsOnArea(ga, osa);
			pwqPathsOnAreaNoLabels(ga, osa, null);
		}

		// check for success
		for (OnePath op : vpaths)
		{
			//assert (op.ciHasrendered >= 2;
			if (op.ciHasrendered < 2)
				TN.emitWarning("ciHasrenderedbad on path:" + vpaths.indexOf(op));
		}

		// labels
		// check any paths if they are now done
		for (OnePath op : vpaths)
		{
			if ((op.linestyle != SketchLineStyle.SLS_CENTRELINE) && (op.plabedl != null) && (op.plabedl.labfontattr != null))
				op.paintLabel(ga, null);
		}
		binpaintWquality = false;
	}

	/////////////////////////////////////////////
	public void ExportSVG(OneTunnel vgsymbols)
	{
		try
		{
			SVGWriter svgwriter = new SVGWriter();//Initilisation should set offsets and scale
			FileAbstraction fpaths = FileAbstraction.MakeWritableFileAbstraction("paths.svg");
			LineOutputStream losp = new LineOutputStream(fpaths);
     	 	svgwriter.SVGPaths(losp, vpaths);
			FileAbstraction fareas = FileAbstraction.MakeWritableFileAbstraction("areas.svg");
			LineOutputStream losa = new LineOutputStream(fareas);
     	 	svgwriter.SVGAreas(losa, vsareas);
			FileAbstraction fsymbols = FileAbstraction.MakeWritableFileAbstraction("symbols.svg");
			LineOutputStream loss = new LineOutputStream(fsymbols);
     	 	svgwriter.SVGSymbols(loss, vgsymbols);	
			FileAbstraction fview = FileAbstraction.MakeWritableFileAbstraction("view.svg");
			LineOutputStream losv = new LineOutputStream(fview);
     	 	svgwriter.SVGView(losv, vgsymbols, vpaths, vsareas, true, true);				
		}
		catch(Exception e)
		{
			TN.emitMessage("Writing Failed! ");
		}
   }


	/////////////////////////////////////////////
//	public void paintSVG(LineOutputStream los, boolean bHideCentreline, boolean bHideMarkers, boolean bHideStationNames, OneTunnel vgsymbols)
//	{
//		assert OnePathNode.CheckAllPathCounts(vnodes, vpaths);
//		binpaintWquality = true; 
//
//		svg = new Svg(los);

		// set up the has rendered flags to begin with
//		for (int i = 0; i < vsareas.size(); i++)
//			((OneSArea)vsareas.elementAt(i)).bHasrendered = false;
//		for (int i = 0; i < sksya.vconncom.size(); i++)
//			((ConnectiveComponentAreas)sksya.vconncom.elementAt(i)).bHasrendered = false;
//		for (int i = 0; i < vnodes.size(); i++)
//			((OnePathNode)vnodes.get(i)).pathcountch = 0;  // count these up as we draw them
//
//		//Initiate SVG file
//		svg.initialise();
//
//		// go through the paths and render those at the bottom here and aren't going to be got later
//		svg.WritePathsNonAreaNoLabels(vpaLineOutputStreamths, bHideCentreline);
//
//		// go through the areas and complete the paths as we tick them off.
//		svg.WriteAreas(vsareas);
//
//		// check for success
//		for (OnePath op : vpaths)
//			assert op.ciHasrendered >= 2;
//
//		// draw all the station names inactive
//		if (!bHideStationNames)
//		{
//			ga.setStroke(SketchLineStyle.linestylestrokes[SketchLineStyle.SLS_DETAIL]);
//			ga.setColor(SketchLineStyle.linestylecolprint);
//			for (int i = 0; i < vnodes.size(); i++)
//			{
//				OnePathNode opn = vnodes.get(i);
//				if (opn.IsCentrelineNode())
//				{
//					if (!bRestrictSubsetCode || (opn.icnodevisiblesubset != 0))
//						ga.drawString(opn.pnstationlabel, (float)opn.pn.getX() + SketchLineStyle.strokew * 2, (float)opn.pn.getY() - SketchLineStyle.strokew);
//				}
//			}
//		}
//
//		// labels
//		// check any paths if they are now done
//		for (OnePath op : vpaths)
//		{
//			if ((op.linestyle != SketchLineStyle.SLS_CENTRELINE) && (op.plabedl != null) && (op.plabedl.labfontattr != null))
//				op.paintLabel(ga, null);
//		}
//		binpaintWquality = false;
//	}
	/////////////////////////////////////////////
	public void paintWbkgd(GraphicsAbstraction ga, boolean bHideCentreline, boolean bHideMarkers, int stationnamecond, OneTunnel vgsymbols, List<OnePath> tsvpathsviz)
	{
		// draw all the paths inactive.
		for (OnePath op : tsvpathsviz)
		{
			if (!bHideCentreline || (op.linestyle != SketchLineStyle.SLS_CENTRELINE))
			{
				boolean bIsSubsetted = (!bRestrictSubsetCode || op.bpathvisiblesubset); // we draw subsetted kinds as quality for now
				op.paintW(ga, bIsSubsetted, false);
			}
		}

		// draw all the nodes inactive
		if (!bHideMarkers)
		{
			for (OnePathNode opn : vnodes)
			{
				if (!bRestrictSubsetCode || (opn.icnodevisiblesubset != 0))
					ga.drawShape(opn.Getpnell(), SketchLineStyle.pnlinestyleattr);
			}
		}

		// draw all the station names inactive
		if (stationnamecond != 0)
		{
			//ga.setStroke(SketchLineStyle.linestylestrokes[SketchLineStyle.SLS_DETAIL]);
			//ga.setColor(SketchLineStyle.fontcol);
			//ga.setFont(SketchLineStyle.defaultfontlab);
			for (OnePathNode opn : vnodes)
			{
				if (opn.IsCentrelineNode())
				{
					if (!bRestrictSubsetCode || (opn.icnodevisiblesubset != 0))
					{
						String slab = (stationnamecond == 2 ? String.valueOf((int)(opn.zalt * 0.1)) : opn.pnstationlabel);
						ga.drawString(slab, SketchLineStyle.stationPropertyFontAttr, (float)opn.pn.getX() + SketchLineStyle.strokew * 2, (float)opn.pn.getY() - SketchLineStyle.strokew);
					}
				}
			}
		}

		// render all the symbols without clipping.
		for (OnePath op : tsvpathsviz)
		{
			if (!bRestrictSubsetCode || op.bpathvisiblesubset)
			{
				for (OneSSymbol oss : op.vpsymbols)
					oss.paintW(ga, false, false);
			}
		}

		// shade in the areas according to depth
		for (OneSArea osa : vsareas)
		{
			assert osa.subsetattr != null;
			if ((!bRestrictSubsetCode || osa.bareavisiblesubset) && (osa.subsetattr.areacolour != null))
				ga.fillArea(osa, osa.zaltcol == null ? osa.subsetattr.areacolour : osa.zaltcol);
		}
	}
};


