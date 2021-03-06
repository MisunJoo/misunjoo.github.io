package project.ffboard.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import project.ffboard.dto.*;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ArticleDao {
    private NamedParameterJdbcTemplate jdbc;
    private JdbcTemplate originJdbc;
    private SimpleJdbcInsert insertActionArticle;
    private SimpleJdbcInsert insertActionArticleContent;
    private SimpleJdbcInsert insertActionFile;
    private SimpleJdbcInsert insertActionArticleCounting;

    public ArticleDao(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
        this.originJdbc = new JdbcTemplate(dataSource);
        this.insertActionArticle = new SimpleJdbcInsert(dataSource).withTableName("article").usingGeneratedKeyColumns("id")
                .usingColumns("title","nick_name","group_id","depth_level","group_seq","category_id", "ip_address","member_id");
        this.insertActionArticleContent = new SimpleJdbcInsert(dataSource).withTableName("article_content");
        this.insertActionFile = new SimpleJdbcInsert(dataSource).withTableName("file");
        this.insertActionArticleCounting = new SimpleJdbcInsert(dataSource).withTableName("article_counting");
    }

    public int arrangeGroupSeq(Long groupId, int groupSeq){
        String sql = "UPDATE article SET group_seq = group_seq + 1 WHERE group_id = :groupId AND group_seq >= :groupSeq";
        Map<String, Number> map = new HashMap<>();
        map.put("groupId", groupId);
        map.put("groupSeq", groupSeq);
        return jdbc.update(sql, map);
    }

    public Long insertArticle(Article article) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(article);
        return insertActionArticle.executeAndReturnKey(params).longValue();
    }

    public void insertGroupId() {
        String sql = "UPDATE article SET group_id=(SELECT LAST_INSERT_ID()) WHERE id=(SELECT LAST_INSERT_ID())";
        originJdbc.execute(sql);
    }

    public int insertArticleContent(ArticleContent articleContent) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(articleContent);
        return insertActionArticleContent.execute(params);
    }

    public int insertArticleCount(ArticleCounting articleCounting){
        SqlParameterSource params = new BeanPropertySqlParameterSource(articleCounting);
        return insertActionArticleCounting.execute(params);
    }

    public int insertFileInfo(Map<String, Object> fileInfo) {
        SqlParameterSource params = new MapSqlParameterSource(fileInfo);
        return insertActionFile.execute(params);
    }

    public ArticleFile getFileInfo(Long articleId) {
        String sql = "SELECT article_id, stored_name, origin_name, content_type, size, path FROM file " +
                "WHERE article_id = :articleId";
        try{
            RowMapper<ArticleFile> rowMapper = BeanPropertyRowMapper.newInstance(ArticleFile.class);
            Map<String, Long> params = Collections.singletonMap("articleId", articleId);
            return jdbc.queryForObject(sql, params, rowMapper);
        }catch (DataAccessException e) {
            return null;
        }
    }

    public int increaseHitCount(Long id){
        String sql = "UPDATE article SET hit = hit + 1 WHERE id = :id";
        Map<String, Long> map = Collections.singletonMap("id", id);
        return jdbc.update(sql, map);
    }

    public int deleteArticle(Long id) {
        String sql = "UPDATE article SET is_deleted=TRUE WHERE id = :id";
        Map<String, Long> map = Collections.singletonMap("id",id);
        return jdbc.update(sql, map);
    }

    public Long updateArticle(Article article) {
        String sql = "UPDATE article SET title = :title, nick_name=:nickName, upddate = :upddate, ip_address = :ipAddress " +
                "WHERE id = :id";
        SqlParameterSource params = new BeanPropertySqlParameterSource(article);
        jdbc.update(sql, params);
        return article.getId();
    }

    public int updateArticleContent(ArticleContent articleContent) {
        String sql = "UPDATE article_content SET content = :content WHERE article_id = :articleId";
        SqlParameterSource params = new BeanPropertySqlParameterSource(articleContent);
        return jdbc.update(sql, params);
    }

    public int updateArticleCount(ArticleCounting articleCounting){
        String sql = "UPDATE article_counting SET count = count + 1 WHERE category_id = :categoryId";
        SqlParameterSource params = new BeanPropertySqlParameterSource(articleCounting);
        return jdbc.update(sql, params);
    }

    public Article getArticle(Long id) {
        String sql = "SELECT id,title,hit,nick_name,group_id,depth_level,group_seq,regdate,"
                +"upddate,category_id,ip_address,member_id,is_deleted "
                +"FROM article WHERE id=:id";
        try{
            RowMapper<Article> rowMapper = BeanPropertyRowMapper.newInstance(Article.class);
            Map<String, Long> params = Collections.singletonMap("id", id);
            return jdbc.queryForObject(sql, params, rowMapper);
        }catch(DataAccessException e){
            return null;
        }
    }

    public ArticleContent getArticleContent(Long id) {
        String sql = "SELECT article_id, content FROM article_content WHERE article_id=:articleId";

        try {
            RowMapper<ArticleContent> rowMapper = BeanPropertyRowMapper.newInstance(ArticleContent.class);
            Map<String, Long> params = Collections.singletonMap("articleId", id);
            return jdbc.queryForObject(sql, params, rowMapper);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<Article> getArticleList(int categoryId, int page, int posts) {
        String sql = "SELECT id,title,hit,nick_name,group_id,depth_level,group_seq,regdate,"
                +"upddate,category_id,ip_address,member_id,is_deleted FROM article WHERE category_id=:categoryId "
                +"ORDER BY group_id DESC, group_seq ASC LIMIT :start , :end";
        RowMapper<Article> rowMapper =  BeanPropertyRowMapper.newInstance(Article.class);

        int start = page * posts - (posts - 1) -1;
        int end = posts;
        Map<String, Integer> params = new HashMap();
        params.put("categoryId", categoryId);
        params.put("start", start);
        params.put("end", end);
        try {
            return jdbc.query(sql,params,rowMapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //article_counting의 count 검색
    public ArticleCounting getCategoryCount(int categoryId){
        String sql = "SELECT count FROM article_counting WHERE category_id = :categoryId";
        try {
            RowMapper<ArticleCounting> rowMapper = BeanPropertyRowMapper.newInstance(ArticleCounting.class);
            Map<String, Integer> map = Collections.singletonMap("categoryId", categoryId);
            return jdbc.queryForObject(sql, map, rowMapper);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int getCount(int categoryId){
        String sql = "SELECT count FROM article_counting WHERE category_id = :categoryId";
        Map<String, Integer> map = Collections.singletonMap("categoryId", categoryId);

        return jdbc.queryForObject(sql, map, new RowMapper<Integer>(){
            @Override
            public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getInt(1);
            }
        });
    }

    /**
     * 검색어를 적용한 게시판 리스트
     * @param categoryId 검색을 원하는 카테고리의 index id
     * @param start 검색을 시작할 인덱스
     * @param limit 검색 리스트의 한도 갯수
     * @param searchType 검색타입으로 "제목","내용","이름","제목+내용"만을 받는다.
     * @param searchWord 사용자가 입력한 검색어
     * @return
     */
    public List<Article> getArticleList(int categoryId, int start, int limit, String searchType, String searchWord) {
        searchWord = "%" + searchWord + "%";
        RowMapper<Article> rowMapper =  BeanPropertyRowMapper.newInstance(Article.class);
        String sql = "SELECT art.id,art.title, art.hit,art.nick_name, art.group_id, art.depth_level, art.group_seq, "
                +"art.regdate, art.upddate, art.category_id, art.ip_address, art.member_id, art.is_deleted, artcon.content "
                +"FROM article art LEFT OUTER JOIN article_content artcon ON art.id = artcon.article_id  WHERE art.category_id=:categoryId AND ";

        if (searchType.equals("제목")) {
            sql += "art.title LIKE :searchWord ";
        } else if (searchType.equals("내용")) {
            sql += "artcon.content LIKE :searchWord ";
        } else if (searchType.equals("이름")) {
            sql += "art.nick_name LIKE :searchWord ";
        } else if (searchType.equals("제목+내용")) {
            sql += "art.title LIKE :searchWord OR artcon.content LIKE :searchWord ";
        } else {
            return null;
        }

        sql+="ORDER BY art.group_id DESC, art.group_seq ASC LIMIT :start , :limit";

        Map<String, Object> params = new HashMap();
        params.put("categoryId", Integer.valueOf(categoryId));

        params.put("start", Integer.valueOf(start));
        params.put("limit", Integer.valueOf(limit));
        params.put("searchWord", searchWord);

        try {
            return jdbc.query(sql,params,rowMapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    } // getArticleList

    //게시판 네비게이션을 위한 카테고리 목록 가져오기
    public List<Category> getCategoryList() {
        String sql = "SELECT id,name FROM category";

        try{
            RowMapper<Category> rowMapper =  BeanPropertyRowMapper.newInstance(Category.class);
            return jdbc.query(sql,rowMapper);
        }catch(DataAccessException e){
            return null;
        }
    }




}
