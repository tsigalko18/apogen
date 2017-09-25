package abstractdt;

import java.util.List;

public class Cluster {

	String master;
	List<String> slaves;
	List<Getter> diffs;
	
	public Cluster(String master, List<String> slaves, List<Getter> diffs) {
		super();
		this.master = master;
		this.slaves = slaves;
		this.diffs = diffs;
	}
	
	public Cluster() {
		super();
	}

	/**
	 * @return the master
	 */
	public String getMaster() {
		return master;
	}
	/**
	 * @param master the master to set
	 */
	public void setMaster(String master) {
		this.master = master;
	}
	/**
	 * @return the slaves
	 */
	public List<String> getSlaves() {
		return slaves;
	}
	/**
	 * @param slaves the slaves to set
	 */
	public void setSlaves(List<String> slaves) {
		this.slaves = slaves;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Cluster [" + (master != null ? "master=" + master + ", " : "")
				+ (slaves != null ? "slaves=" + slaves + ", " : "")
				+ (diffs != null ? "diffs=" + diffs : "") + "]";
	}

	/**
	 * @return the diffs
	 */
	public List<Getter> getDiffs() {
		return diffs;
	}
	
	/**
	 * @param diffs the diffs to set
	 */
	public void setDiffs(List<Getter> diffs) {
		this.diffs = diffs;
	}
}
