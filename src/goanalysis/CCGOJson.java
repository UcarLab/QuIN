package goanalysis;

public class CCGOJson extends GOJson {

	private int[] ccids;
	
	public CCGOJson(int[] iccids, String[] gids, String[] t, int[] git, int[] gic,
			String[] e, String[] f) {
		super(gids, t, git, gic, e, f);
		ccids = iccids;
	}

	public int[] getCCIds(){
		return ccids;
	}
	
}
