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

import java.io.File; 
import java.io.IOException;  

//
//
// TNXML
//
//


/////////////////////////////////////////////
// constants
/////////////////////////////////////////////
class TNXML
{
	static String sHEADER = "<?xml version='1.0' encoding='us-ascii'?>"; 

	static String sTUNNELXML = "tunnelxml"; 

	static String sSET = "set"; 
	static String sFLOAT_VALUE = "fval"; 
	static String sTEXT = "text"; 
	static String sNAME = "name"; 


	static String sEXPORTS = "exports"; 
	static String sEXPORT = "export"; 
		static String sEXPORT_FROM_STATION = "estation"; 
		static String sEXPORT_TO_STATION = "ustation"; 

	static String sMEASUREMENTS = "measurements"; 
		static String sSVX_DATE = "date"; 
		static String sSVX_TITLE = "title"; 
		static String sSVX_TAPE_PERSON = "tapeperson"; 

	static String sLEG = "leg"; // effectively the same as set
	static String sFIX = "fix"; 
	static String sPOS_FIX = "pos_fix"; 
		static String sFROM_STATION = "from"; 
		static String sTO_STATION = "to"; 

		static String sTAPE = "tape"; 
		static String sCOMPASS = "compass"; 
		static String sCLINO = "clino"; 


	// additional tube modelling stuff  
	static String sXSECTION = "xsection"; 
	static String sXSECTION_INDEX = "xsind"; // prefer to be the index.  
		static String sXS_STATION0 = "xsst0"; 
		static String sXS_STATION1 = "xsst1"; 
		static String sXS_STATION_LAM = "xs_lam"; 
		static String sXS_STATION_ORIENT_FORE = "xsstorfore"; 
		static String sXS_STATION_ORIENT_BACK = "xsstorback"; 
		static String sXS_STATION_ORIENT_REL_COMPASS = "xsstorrelc"; 
		static String sXS_STATION_ORIENT_CLINO = "xsstorclin"; 

	static String sLINEAR_TUBE = "ltube"; 
		static String sFROM_XSECTION = "xsfrom"; 
		static String sTO_XSECTION = "xsto"; 


	static String sSKETCH = "sketch"; 

	static String sSKETCH_BACK_IMG = "backimage"; 
		static String sSKETCH_BACK_IMG_FILE = "imgfile"; 

		static String sAFFINE_TRANSFORM = "affinetrans"; 
			static String sAFTR_M00 = "aftrm00"; 
			static String sAFTR_M01 = "aftrm10"; 
			static String sAFTR_M10 = "aftrm01"; 
			static String sAFTR_M11 = "aftrm11"; 
			static String sAFTR_M20 = "aftrm20"; 
			static String sAFTR_M21 = "aftrm21"; 

	static String sSKETCH_PATH = "skpath"; 
		static String sFROM_SKNODE = "from";
		static String sTO_SKNODE = "to";

		static String sSK_LINESTYLE = "linestyle";

			// values of linestyle.
			static String vsLS_CENTRELINE = "centreline";
			static String vsLS_WALL = "wall";
			static String vsLS_ESTWALL = "estwall";
			static String vsLS_PITCHBOUND = "pitchbound";
			static String vsLS_CEILINGBOUND = "ceilingbound";
			static String vsLS_DETAIL = "detail";
			static String vsLS_INVISIBLE = "invisible";
			static String vsLS_CONNECTIVE = "connective";
			static String vsLS_FILLED = "filled";

		// state applied to a linestyle.
		static String sSPLINED = "splined";

		static String sLABEL = "label";
			static String sTAIL = "tail";
			static String sHEAD = "head";
			static String sSPREAD = "spread";

	// the new symbol stuff laid out inside the label
	static String sLSYMBOL = "symbol";
		static String sLSYMBOL_NAME = "name";
		static String sLMCODE = "mcode";
		static String sLQUANTITY = "qty";

	static String sPOINT = "pt";
		static String sPTX = "X";
		static String sPTY = "Y";
		static String sPTZ = "Z";


	static String[] tabs = { "", "\t", "\t\t", "\t\t\t", "\t\t\t\t" };


	/////////////////////////////////////////////
	static String EncodeLinestyle(int linestyle)
	{
		switch (linestyle)
		{
			case SketchLineStyle.SLS_CENTRELINE:
				return vsLS_CENTRELINE;
			case SketchLineStyle.SLS_WALL:
				return vsLS_WALL;
			case SketchLineStyle.SLS_ESTWALL:
				return vsLS_ESTWALL;
			case SketchLineStyle.SLS_PITCHBOUND:
				return vsLS_PITCHBOUND;
			case SketchLineStyle.SLS_CEILINGBOUND:
				return vsLS_CEILINGBOUND;
			case SketchLineStyle.SLS_DETAIL:
				return vsLS_DETAIL;
			case SketchLineStyle.SLS_INVISIBLE:
				return vsLS_INVISIBLE;
			case SketchLineStyle.SLS_CONNECTIVE:
				return vsLS_CONNECTIVE;
			case SketchLineStyle.SLS_FILLED:
				return vsLS_FILLED;
			default:
				TN.emitError("Unknown linestyle");
		}
		return "??";
	}

	/////////////////////////////////////////////
	static int DecodeLinestyle(String slinestyle)
	{
		if (slinestyle.equals(vsLS_CENTRELINE))
			return SketchLineStyle.SLS_CENTRELINE;
		if (slinestyle.equals(vsLS_WALL))
			return SketchLineStyle.SLS_WALL;
		if (slinestyle.equals(vsLS_ESTWALL))
			return SketchLineStyle.SLS_ESTWALL;
		if (slinestyle.equals(vsLS_PITCHBOUND))
			return SketchLineStyle.SLS_PITCHBOUND;
		if (slinestyle.equals(vsLS_CEILINGBOUND))
			return SketchLineStyle.SLS_CEILINGBOUND;
		if (slinestyle.equals(vsLS_DETAIL))
			return SketchLineStyle.SLS_DETAIL;
		if (slinestyle.equals(vsLS_INVISIBLE))
			return SketchLineStyle.SLS_INVISIBLE;
		if (slinestyle.equals(vsLS_CONNECTIVE))
			return SketchLineStyle.SLS_CONNECTIVE;
		if (slinestyle.equals(vsLS_FILLED))
			return SketchLineStyle.SLS_FILLED;

		// backwards compatibility for now.
		TN.emitWarning("numeric linestyle " + slinestyle);
		return Integer.parseInt(slinestyle);
	}

	/////////////////////////////////////////////
	/////////////////////////////////////////////
	static StringBuffer sb = new StringBuffer();

	/////////////////////////////////////////////
	static void sbstartxcom(int indent, String command)
	{
		sb.setLength(0);
		sb.append(tabs[indent]);
		sb.append('<');
		sb.append(command);
	}

	/////////////////////////////////////////////
	static void sbattribxcom(String attr, String val)
	{
		sb.append(" ");
		sb.append(attr);
		sb.append("=\"");

		// there must be a better way here.
		// sb.append(val);
		// substitute problem symbols that the jaxp doesn't like.
		for (int i = 0; i < val.length(); i++)
		{
			switch (val.charAt(i))
			{
			case '\\':
				sb.append('/');
				break;
			case '&':
				sb.append(".and.");
				break;
			default:
				sb.append(val.charAt(i));
			}
		}

		sb.append("\"");
	}

	/////////////////////////////////////////////
	static String convertback(String val)
	{
		return val;
	}

	/////////////////////////////////////////////
	static String sbendxcomsingle()
	{
		sb.append("/>");
		return sb.toString();
	}

	/////////////////////////////////////////////
	static String sbendxcom()
	{
		sb.append(">");
		return sb.toString();
	}

	/////////////////////////////////////////////
	/////////////////////////////////////////////
	static String xcom(int indent, String command, String attr0, String val0)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		return sbendxcomsingle();
	}

	/////////////////////////////////////////////
	static String xcom(int indent, String command, String attr0, String val0, String attr1, String val1)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		return sbendxcomsingle();
	}

	/////////////////////////////////////////////
	static String xcom(int indent, String command, String attr0, String val0, String attr1, String val1, String attr2, String val2)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		sbattribxcom(attr2, val2);
		return sbendxcomsingle();
	}


	/////////////////////////////////////////////
	static String xcomopen(int indent, String command)
	{
		sbstartxcom(indent, command);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0, String attr1, String val1)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0, String attr1, String val1, String attr2, String val2)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		sbattribxcom(attr2, val2);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0, String attr1, String val1, String attr2, String val2, String attr3, String val3)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		sbattribxcom(attr2, val2);
		sbattribxcom(attr3, val3);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0, String attr1, String val1, String attr2, String val2, String attr3, String val3, String attr4, String val4)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		sbattribxcom(attr2, val2);
		sbattribxcom(attr3, val3);
		sbattribxcom(attr4, val4);
		return sbendxcom();
	}

	/////////////////////////////////////////////
	static String xcomopen(int indent, String command, String attr0, String val0, String attr1, String val1, String attr2, String val2, String attr3, String val3, String attr4, String val4, String attr5, String val5)
	{
		sbstartxcom(indent, command);
		sbattribxcom(attr0, val0);
		sbattribxcom(attr1, val1);
		sbattribxcom(attr2, val2);
		sbattribxcom(attr3, val3);
		sbattribxcom(attr4, val4);
		sbattribxcom(attr5, val5);
		return sbendxcom();
	}


	/////////////////////////////////////////////
	static String xcomclose(int indent, String command)
	{
		sb.setLength(0);
		sb.append(tabs[indent]);
		sb.append('<');
		sb.append('/');
		sb.append(command);
		return sbendxcom();
	}


	/////////////////////////////////////////////
	static String xcomtext(String command, String text)
	{
		return "<" + command + ">" + text + "</" + command + ">";
	}


	/////////////////////////////////////////////
	// quick and dirty extraction here.  (the two command things could be buffered).
	static String xrawextracttext(String source, String commandopen, String commandclose)
	{
		int p0 = source.indexOf(commandopen);
		int p0g = p0 + commandopen.length();
		int p1 = source.lastIndexOf(commandclose);

		if ((p0 != -1) && (p1 != -1) && (p0g < p1))
			return source.substring(p0g, p1);
		return null;
	}

	/////////////////////////////////////////////
	static String xrawextracttext(String source, String command)
	{
		return xrawextracttext(source, xcomopen(0, command), xcomclose(0, command));
	}

	/////////////////////////////////////////////
	// this is very brittle stuff to extract one closed command
	static String xrawextractattr(String source, String[] val, String command, String[] attr)
	{
		int pe = source.indexOf("/>");
		int ps = source.indexOf(command);
		if ((pe == -1) || (ps == -1) || (pe <= ps))
			return null;
		for (int i = 0; i < attr.length; i++)
		{
			int pa = source.indexOf(attr[i]);
			val[i] = null;
			if ((pa != -1) && (pa < pe))
			{
				int pq1 = source.indexOf("\"", pa);
				int pq2 = source.indexOf("\"", pq1 + 1);
				if ((pq1 < pq2) && (pq2 < pe))
					val[i] = source.substring(pq1 + 1, pq2);
			}
		}
		return source.substring(pe + 2).trim();
	}
};
