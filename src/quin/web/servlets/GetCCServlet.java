package quin.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quin.filereader.ChIAPETRead;
import quin.network.db.query.CCQuery;
import quin.network.db.query.InteractionQuery;
import quin.web.UserSession;
import quin.web.networkjson.Edge;
import quin.web.networkjson.Network;
import quin.web.networkjson.Node;
import quin.web.networkjson.SupportingEdge;

import com.google.gson.Gson;

import db.SQLConnectionFactory;

public class GetCCServlet extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException{
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {		

		Connection conn = SQLConnectionFactory.getConnection();
		try{
			ServletUtil util = new ServletUtil();
			
			UserSession us = new UserSession();
			long uid = -1;
			try {
				uid = us.getUserId(req, resp, conn);
			} catch (Exception e) {
				util.setResponse(resp, "[\"Error: Error loading session data.\"]");
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				return;
			}
			
			String network = req.getParameter("network");
			long fid = -1;
			try {
				fid = Long.parseLong(network);
				Util u = new Util();
				if(!u.dataexists(conn, "usersessions.Networks", uid, fid)){
					util.setResponse(resp, "[\"Error: Error loading network.\"]");
					try {
						conn.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					return;
				}
			}
			catch(Exception e){
				util.setResponse(resp, "[\"Error: Error loading network.\"]");
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				return;
			}
			
			String[] genelists = req.getParameterValues("genes[]");
			String[] diseaselists = req.getParameterValues("diseases[]");
			String[] regionlists = req.getParameterValues("regions[]");
			String[] snplists = req.getParameterValues("snps[]");
			
	
	
	
			//String traitsrc = req.getParameter("traitsrc");
			//int ts = Integer.parseInt(traitsrc);
			int ts = 2; //Just GWAS for now
			String sccid = req.getParameter("ccid");
			int ccid = Integer.parseInt(sccid);
			
			//boolean promoter = req.getParameter("promoter").equals("true");
			
			Network njson = new Network();
			njson.setNodes(new Node[0]);
			njson.setEdges(new Edge[0]);
	
			SIIndexUtil siu = new SIIndexUtil();
			Integer[] sids = siu.getIndices(conn, uid, fid, ts, genelists, diseaselists, regionlists, snplists);
			CCQuery ccq = new CCQuery();
			try {
				njson = ccq.getCC(conn, "chiapet", fid, ccid, 2000, 2000, sids);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
	
			//4-15-2016 New code to visualize supporting edge via data of other networks
			String[] supportedges = req.getParameterValues("supportingedges[]");
			long[] seids = siu.getSupportingEdgeIds(conn, uid, supportedges);
			njson.setSupportingEdges(getSupportingEdges(conn, njson.getNodes(), seids));
	
			
			Gson gson = new Gson();
			resp.setContentType("application/json");
			PrintWriter out = resp.getWriter();
			out.print(gson.toJson(njson, Network.class));
			out.flush();
			
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private SupportingEdge[][] getSupportingEdges(Connection conn, Node[] nodes, long[] dids){
		LinkedList<SupportingEdge[]> rv = new LinkedList<SupportingEdge[]>();
		for(int i = 0; i < dids.length; i++){
			try {
				rv.add(getSupportingEdges(conn, nodes, dids[i]));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rv.toArray(new SupportingEdge[0][]);
	}
	
	private SupportingEdge[] getSupportingEdges(Connection conn, Node[] nodes, long did) throws SQLException{
		InteractionQuery iq = new InteractionQuery();
		ChIAPETRead[] interactions = iq.getInteractions(conn, did);
		
		TreeMap<Integer, Set<Integer>> tm = new TreeMap<Integer, Set<Integer>>();
		//TODO could speed this up slightly by sorting by chromosome and start position and scanning both simultaneously, but just trying to get this to work as fast as possible
		for(int i = 0; i < nodes.length; i++){
			for(int j = 0; j < interactions.length; j++){
				if(nodes[i].getChr().equals(interactions[j].getLChr()) && nodes[i].getEnd() >= interactions[j].getLStart() && nodes[i].getStart() <= interactions[j].getLEnd()){
					if(!tm.containsKey(j)){
						tm.put(j, new TreeSet<Integer>());
					}
					tm.get(j).add(nodes[i].getId());
				}
				else if(nodes[i].getChr().equals(interactions[j].getRChr()) && nodes[i].getEnd() >= interactions[j].getRStart() && nodes[i].getStart() <= interactions[j].getREnd()){
					if(!tm.containsKey(j)){
						tm.put(j, new TreeSet<Integer>());
					}
					tm.get(j).add(nodes[i].getId());
				}
			}
		}
		
		Set<String> dupfilter = new TreeSet<String>();
		LinkedList<SupportingEdge> rv = new LinkedList<SupportingEdge>();
		for(Iterator<Entry<Integer,Set<Integer>>> it = tm.entrySet().iterator(); it.hasNext();){
			Entry<Integer,Set<Integer>> next = it.next();
			int iid = next.getKey();
			Integer[] nids = next.getValue().toArray(new Integer[0]);
			if(nids.length > 1){
				for(int i = 0; i < nids.length; i++){
					for(int j = i+1; j < nids.length; j++){
						String dupstring = nids[i]+"|"+nids[j];
						if(!dupfilter.contains(dupstring)){
							rv.add(new SupportingEdge(iid, nids[i], nids[j], did));
						}
						dupfilter.add(dupstring);
					}
				}
			}
		}
		return rv.toArray(new SupportingEdge[0]);
	}
	
}
