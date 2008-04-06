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

import java.util.List;
import java.util.ArrayList;



// classes to be calculated from the equate array.
/////////////////////////////////////////////
class Eq
{
	String eqstationname = null;
	OneTunnel eqtunnel;
	Eq eqlink = this;

	Eq(OneTunnel leqtunnel, String leqstationname)
	{
		eqtunnel = leqtunnel;
		eqstationname = leqstationname;
	}

	public String toString()
	{
		return eqtunnel.fullname + "  " + eqstationname;
	}
}


/////////////////////////////////////////////
class EqVec
{
	List<Eq> eqlist = new ArrayList<Eq>();
	OneTunnel eqtunnelroot = null;


	/////////////////////////////////////////////
	Eq FindEq(OneTunnel leqtunnel, int icode)
	{
		if (leqtunnel == null)
			TN.emitWarning("Bad Find equ code:" + String.valueOf(icode));

		for (Eq eq : eqlist)
		{
			if (eq.eqtunnel == leqtunnel)
				return eq;
		}
		return null;
	}

	/////////////////////////////////////////////
	void AddEquateValueEq(Eq eqval)
	{
		// build in the link
		Eq eql = FindEq(eqval.eqtunnel, 1);
		if (eql != null)
		{
			eqval.eqlink = eql.eqlink;
			eql.eqlink = eqval;
		}

		// update the root value
		if (!eqlist.isEmpty())
		{
			// we keep moving root up till we score a hit.
			while (true)
			{
				if (eqtunnelroot == null)
				{
					TN.emitError("eq overflow on " + eqval.toString());
					DumpOut();
					break;
				}

				OneTunnel eqtscan = eqval.eqtunnel;
				while (eqtscan != null)
				{
					if (eqtscan == eqtunnelroot)
						break;
					eqtscan = eqtscan.uptunnel;
				}

				if (eqtscan != null)
					break;
				eqtunnelroot = eqtunnelroot.uptunnel;
			}
		}
		else
			eqtunnelroot = eqval.eqtunnel;

		// put it into the array
		eqlist.add(eqval);
	}

	/////////////////////////////////////////////
	Eq AddEquateValue(OneTunnel leqtunnel, String leqstationname)
	{
		if (leqtunnel == null)
			TN.emitError("how?");

		// first derive a unique name
		String uniquename = leqstationname;
		for (int n = 1; true; n++)
		{
			int i;
			for (i = leqtunnel.stationnames.size() - 1; i >= 0; i--)
			{
				if (uniquename.equalsIgnoreCase(leqtunnel.stationnames.get(i)))
					break;
			}
			if (i == -1)
				break;

			uniquename = leqstationname + "_" + String.valueOf(n);
		}

		// now apply it
		Eq eqval = new Eq(leqtunnel, uniquename);
		AddEquateValueEq(eqval);
		return eqval;
	}


	/////////////////////////////////////////////
	// fill in the missing values
	// this adds values into the array which later gets extended
	boolean MakeEquateLine(Eq eqval)
	{
		if (eqval.eqtunnel != eqtunnelroot)
		{
			if (eqval.eqtunnel.uptunnel == null)
			{
				TN.emitError("export overflow");
				return false;
			}

			Eq equp = FindEq(eqval.eqtunnel.uptunnel, 2);
			if (equp == null)
			{
				String exprefix = (eqval.eqtunnel.name.length() != 0 ? eqval.eqtunnel.name + TN.ExportDelimeter : "");
				equp = AddEquateValue(eqval.eqtunnel.uptunnel, exprefix + eqval.eqstationname);
				equp.eqtunnel.stationnames.add(equp.eqstationname);
			}
			eqval.eqtunnel.vexports.add(new OneExport(eqval.eqstationname, equp.eqstationname));
		}
		return true;
	}

	/////////////////////////////////////////////
	// move the root up one spot
	void ExtendRootIfNecessary()
	{
		// extend it if the root equate value is not unique
		Eq eqroot = FindEq(eqtunnelroot, 3);
		if (eqroot == null)
			return;
		if (eqroot.eqlink == eqroot)  // equate to self
			return;

			//assert eqtunnelroot.uptunnel != null;  // not entirely sure how this function works, but this fails and maybe would always(?)
			//assert eqtunnelroot.uptunnel.name != null;
			//assert eqroot.eqstationname != null;
System.out.println("extendingroot " + eqtunnelroot.uptunnel.name);
		if ((eqtunnelroot.uptunnel != null) && (eqtunnelroot.uptunnel.name != null) && (eqroot.eqstationname != null))
			AddEquateValue(eqtunnelroot.uptunnel, eqtunnelroot.name + TN.ExportDelimeter + eqroot.eqstationname);
		else
			TN.emitWarning("Bad Extendrootifnecessary");
	}


	/////////////////////////////////////////////
	void DumpOut()
	{
		if (eqtunnelroot != null)
			TN.emitWarning("roottunn: " + eqtunnelroot.name);
		for (Eq eq : eqlist)
			TN.emitWarning(eq.toString());
	}
}

