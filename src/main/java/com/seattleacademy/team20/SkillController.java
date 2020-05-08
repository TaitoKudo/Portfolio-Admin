package com.seattleacademy.team20;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Handles requests for the application home page.
 */
@Controller
public class SkillController {
  private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
  //MySQLと接続にsql文を流して取得する必要があるからこれを書き込む（定義する）
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping(value = "/upload", method = RequestMethod.GET)
  public String skillUpload(Locale locale, Model model) {
    logger.info("Welcome home! The client locale is {}.", locale);
    try {
      intialize();
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
    List<Skill> skills = selectSkills();
    uploadSkill(skills);
    return "skillUpload";
  }

  //	Listの宣言
  public List<Skill> selectSkills() {
    //		sequel proで作ったテーブルからデータを取得する文字列を取得する文字列をsqlという変数に入れている
    final String sql = "select * from skills";
    //		おそらくJdbcTemplateでsql実行している
    //		RowMapperに返却されたSkillに返却されている？
    return jdbcTemplate.query(sql, new RowMapper<Skill>() {
      //呪文
      public Skill mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Skill(rs.getString("category"), rs.getString("name"), rs.getInt("score"));
      }
    });
  }

  //特定	メンバ変数
  private FirebaseApp app;

  //SDKの初期化（最初の値をセットする　代入　データベースの値を埋めている）
  public void intialize() throws IOException {
    FileInputStream refreshToken = new FileInputStream(
        "/Users/taitokudo/Downloads/dev-portfolio-1127f-firebase-adminsdk-macbl.json");
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(GoogleCredentials.fromStream(refreshToken))
        .setDatabaseUrl("https://dev-portfolio-1127f.firebaseio.com/")
        .build();
    app = FirebaseApp.initializeApp(options, "others");
  }
  //ここまでメソッド箱を定義

  public void uploadSkill(List<Skill> skills) {
    //テータの保存 skillsをrefで定義
    final FirebaseDatabase database = FirebaseDatabase.getInstance(app);
    DatabaseReference ref = database.getReference("sampleSkills");
    //MySQLからデータの取得 DBAcceseSumpleContのしたの部分　
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    Map<String, Object> map;
    Map<String, List<Skill>> skillMap = skills.stream().collect(Collectors.groupingBy(Skill::getCategory));
    for (Map.Entry<String, List<Skill>> entry : skillMap.entrySet()) {
      //			    System.out.println(entry.getKey());
      //			    System.out.println(entry.getValue());
      map = new HashMap<>();
      map.put("category", entry.getKey());
      map.put("skill", entry.getValue());

      dataList.add(map);
    }
    //リアルタイムデータベース更新
    ref.setValue(dataList, new DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError != null) {
          System.out.println("Data could be saved" + databaseError.getMessage());
        } else {
          System.out.println("Data save successfully.");
        }
      }
    });
  }

  //	                 ↓メインクラス名
  public class Skill {

    private String category;
    private String name;
    private int score;

    public Skill(String category, String name, int score) {
      this.category = category;
      this.name = name;
      this.score = score;
    }

    public String getCategory() {
      return category;
    }

    public String getName() {
      return name;
    }

    public int getScore() {
      return score;
    }
  }
}
//データの整形　アップロードする際にrealtime上のデータと合わせるために行う

//realtimedatabaseに実際にアップロードする

//DatabaseReference skillRef = ref.child("skillcategories")

//Map型リストを作る。MapはStringで聞かれたものに対し、Object型で返すようにしている。
//		List<Map<string, Object>> dataList =new ArrayList<Objectring, object>>();
//		Map<STRING, object> dataMap;
//		for(SkillCategory category : categories) {
//			dataMap = new HashMap<>();
//			dataMap.put("category, "category");
//			// skillsのcategoryisとカテゴリのidで同じものをfilterで抽出している
//					//collectはadd的ななにか？わからん
//		// ??????????
//		// steamという方を使って、一致するものを持ってくるようにしている。俺は使わずに一つ抽出
//		}
//		//skillsRef.updateChildrenAsync(dataMap);
//		ref.setValue(dataList, new DatabaseReference.CompletionListener() {
//
//			@Override
//			public void onComplete(DatabaseError error, DatabaseReference ref) {
//				// TODO 自動生成されたメソッド・スタブ
//
//			}
//		});
//	}