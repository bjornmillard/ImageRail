/**  
   ImageRail:
   Software for high-throughput microscopy image analysis

   Copyright (C) 2011 Bjorn Millard <bjornmillard@gmail.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * FeatureSorter.java
 *
 * @author Bjorn Millard
 */

package tools;

import java.io.File;
import java.util.Comparator;

public class ImageFileChannelSorter implements Comparator {
	public int compare(Object p1, Object p2) {
		File f1 = (File) p1;
		File f2 = (File) p2;

		String f1n = f1.getName();
		String f2n = f2.getName();

		int indx1_w = f1n.indexOf("_w");
		int indx2_w = f2n.indexOf("_w");

		String f1w = f1n.substring(indx1_w, f1n.length());
		String f2w = f2n.substring(indx2_w, f2n.length());

		return f1w.compareTo(f2w);

	}
}

