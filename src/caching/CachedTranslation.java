//sg
package caching;
/**This class is used to store the cached translations. 
 * 
 * @author aman
 *
 */
public class CachedTranslation {
	String english;
	String hindi;
	static int idcounter;
	int id;
	public CachedTranslation() {
		
	}
	public CachedTranslation(String english, String hindi) {
		this.english = english;
		this.hindi = hindi;
		this.id = CachedTranslation.idcounter + 1;
		CachedTranslation.idcounter++;
	}
	public String getEnglish() {
		return english;
	}
	public void setEnglish(String english) {
		this.english = english;
	}
	public String getHindi() {
		return hindi;
	}
	public void setHindi(String hindi) {
		this.hindi = hindi;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
