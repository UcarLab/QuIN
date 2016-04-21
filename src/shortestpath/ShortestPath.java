package shortestpath;

public class ShortestPath {

	private String _dataset, _term, _termchr, _termtss, _targetdataset,
			_targetterm, _targetchr, _targettss, _path;
	private int _termstart, _termend, _termtssd, _minnumedges, _distance,
			_targetstart, _targetend, _targettssd, _nodecount, _edgecount,
			_mininter, _maxinter, _ccid;
	private double _avgscore, _minscore, _maxscore, _avginter;

	public ShortestPath(String dataset, String term, String termchr,
			int termstart, int termend, String termtss, int termtssd,
			int minnumedges, int distance, String targetdataset,
			String targetterm, String targetchr, int targetstart,
			int targetend, String targettss, int targettssd, double avgscore,
			double minscore, double maxscore, double avginter, int mininter,
			int maxinter, int nodecount, int edgecount, String path, int ccid) {

		_dataset = dataset;
		_term = term;
		_termchr = termchr;
		_termtss = termtss;
		_targetterm = targetterm;
		_targetchr = targetchr;
		_targettss = targettss;
		_path = path;
		_termstart = termstart;
		_termend = termend;
		_termtssd = termtssd;
		_targetdataset = targetdataset;
		_minnumedges = minnumedges;
		_distance = distance;
		_targetstart = targetstart;
		_targetend = targetend;
		_targettssd = targettssd;
		_nodecount = nodecount;
		_edgecount = edgecount;
		_mininter = mininter;
		_maxinter = maxinter;
		_avgscore = avgscore;
		_minscore = minscore;
		_maxscore = maxscore;
		_avginter = avginter;
		_ccid = ccid;
	}

	public String getDataset() {
		return _dataset;
	}

	public String getTerm() {
		return _term;
	}

	public String getTermChr() {
		return _termchr;
	}

	public String getTermNearestTSS() {
		return _termtss;
	}

	public String getTargetTerm() {
		return _targetterm;
	}

	public String getTargetChr() {
		return _targetchr;
	}

	public String getTargetNearestTSS() {
		return _targettss;
	}

	public String getPath() {
		return _path;
	}

	public int getTermStart() {
		return _termstart;
	}

	public int getTermEnd() {
		return _termend;
	}

	public int getTermTSSDistance() {
		return _termtssd;
	}

	public String getTargetDataset() {
		return _targetdataset;
	}

	public int getMinimumEdgesToTarget() {
		return _minnumedges;
	}

	public int getGenomicDistance() {
		return _distance;
	}

	public int getTargetStart() {
		return _targetstart;
	}

	public int getTargetEnd() {
		return _targetend;
	}

	public int getTargetTSSDistance() {
		return _targettssd;
	}

	public int getComponentNodeCount() {
		return _nodecount;
	}

	public int getComponentEdgeCount() {
		return _edgecount;
	}

	public int getMinInteractions() {
		return _mininter;
	}

	public int getMaxInteractions() {
		return _maxinter;
	}

	public double getAVGScore() {
		return _avgscore;
	}

	public double getMinScore() {
		return _minscore;
	}

	public double getMaxScore() {
		return _maxscore;
	}

	public double getAVGInteractions() {
		return _avginter;
	}
	
	public int getCCId(){
		return _ccid;
	}

}
