import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class PileupReformater {

	static int
		posIdx=1,
		refIdx=2,
		alignIdx=4,
		readIdx=7;
	
	public static void main(String[] args) throws IOException {
		BufferedReader r = new BufferedReader( new FileReader(args[0]) );
		String line;
		HashMap<String,StringBuffer[]> reads = new HashMap<String, StringBuffer[]>();
		HashSet<String> used = new HashSet<String>();
		HashMap<String,int[]> problem = new HashMap<String,int[]>();
		ArrayList<String> position = new ArrayList<String>();
		String prefix = "";
		while( (line=r.readLine())!=null ) {
			String[] split = line.split("\t");
			String pos = split[posIdx];
			String ref = split[refIdx];
			String align = split[alignIdx];
//System.out.println(pos);
			position.add(pos);
			String[] curReads = split[readIdx].split(",");
			int j = 0;
			used.clear();
			for( String cr: curReads ) {
				if( used.contains(cr) ) {
					int[] stat = problem.get(cr);
					if( stat==null ) {
						stat = new int[1];
						problem.put(cr, stat);
					}
					stat[0]++;
				} else {
					used.add(cr);
				}
				
				StringBuffer[] info = reads.get(cr);
				if( info == null ) {
					info = new StringBuffer[2];
					info[0] = new StringBuffer(prefix);
					info[1] = new StringBuffer(prefix);
					reads.put(cr, info);
				}
				int middle=j+1;
				if( align.charAt(j)=='^' ) { //first base of read
					middle+=2; //bug? ^] instead of ^
				}
				char c = middle<align.length() ? align.charAt(middle) : '!';
				int end;
				if( c=='-' || c=='+' ) { //insertion or deletion
					end = middle+1;
					while( end < align.length() && align.substring(end, end+1).matches("\\d") ) {
						end++;
					}
					int l = Integer.parseInt(align.substring(middle+1, end));
//System.out.println( cr + "\t" + split[alignIdx].substring(j, end) + "\t" + l + "\t" + split[alignIdx].substring(j, end+l) );
					end+=l;
				} else {
					end = middle;
				}
				if( end < align.length() && align.charAt(end)=='$') { //last base of read
					end++;
				}
				
				String code = align.substring(j, end);
				info[1].append("\t" + code);
				c = code.charAt(0);
				if( c=='^' ) {
					c = code.charAt(2);
				}
				String base;
				switch( c ) {
					case '.': case',': base = ref; break;
					case '*': case '#': base = "-"; break;
					case '>': case '<': base = "?"; break;
					default: base = code.substring(0, 1).toUpperCase();
				}
				info[0].append("\t" + base);
//System.out.println( cr + "\t" + code + "\t" + base );
				j=end;
			}
			Iterator<String> allReads = reads.keySet().iterator();
			while( allReads.hasNext() ) {
				String curRead = allReads.next();
				if( !used.contains(curRead) ) {
					StringBuffer[] info = reads.get(curRead);
					info[0].append("\t");
					info[1].append("\t");
				}
			}
			
//System.out.println(j == split[4].length());
			prefix += "\t";
		}
		r.close();

		System.out.println("read\t#problems");
		Iterator<Entry<String,int[]>> prob = problem.entrySet().iterator();
		while( prob.hasNext() ) {
			Entry<String,int[]> e = prob.next();
			System.out.println(e.getKey() + "\t" + e.getValue()[0]);
		}
		System.out.println();
		
		
		
		BufferedWriter w = new BufferedWriter( new FileWriter( args[0] + "-refomated.tabular") );
		w.append("read\tproblem");
		for( String p : position ) {
			w.append("\t"+p);
		}
		w.newLine();
		
		int col = 0; //select whether SNPS or code should be returned
		Iterator<Entry<String,StringBuffer[]>> it = reads.entrySet().iterator();
		//HashMap<String,int[]> stat = new HashMap<String,int[]>();
		while( it.hasNext() ) {
			Entry<String,StringBuffer[]> e = it.next();
			boolean pr = problem.containsKey(e.getKey());
			String res = e.getValue()[col].toString();
			w.append( e.getKey() + "\t" + pr + res);
			w.newLine();
			
			/*if( !pr ) {
				int[] count = stat.get( res );
				if( count==null ) {
					count = new int[1];
					stat.put(res, count);
				}
				count[0]++;
			}*/
		}
		w.close();
		
		/*
		Entry[] array = stat.entrySet().toArray(new Entry[0]);
		Arrays.sort(array, new EntryComparator() );
		
		System.out.println("#\tunproblematic SNP-String");
		for( Entry e : array ) {
			System.out.println(((int[])e.getValue())[0] + "\t" + e.getKey() );
		}*/
	}
	
	private static class EntryComparator implements Comparator<Entry> {

		@Override
		public int compare(Entry o1, Entry o2) {
			return Integer.compare( ((int[])o1.getValue())[0], ((int[])o2.getValue())[0] );
		}
	}
}
