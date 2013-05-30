package be.tfe.android.tasks.containers;

public class UserInfo {
	public int id, rank, nbr_user;
	public float score;
	public String nickname;
	public int position_value, position_pm, position_trend;
	public int nbr_group;
	
	public int getId()
	{
		return this.id;
	}

	public float getScore() {
		return score;
	}

	public int getRank()
	{
		return rank;
	}
	
	public int getNbrUser()
	{
		return nbr_user;
	}
	
	public String getNickname()
	{
		return nickname;
	}

	public void setIdUser(int id) {
		this.id = id;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
