package edu.sustech.oj_server.controller;

import edu.sustech.oj_server.dao.ContestDao;
import edu.sustech.oj_server.dao.ProblemDao;
import edu.sustech.oj_server.entity.Contest;
import edu.sustech.oj_server.entity.Problem;
import edu.sustech.oj_server.entity.User;
import edu.sustech.oj_server.util.Authentication;
import edu.sustech.oj_server.util.ReturnListType;
import edu.sustech.oj_server.util.ReturnType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ContestController {

    @Autowired
    ContestDao contestDao;
    @Autowired
    ProblemDao problemDao;

    @RequestMapping("/api/contests")
    public ReturnType<ReturnListType<Contest>> list_contest(@RequestParam("limit") int limit, @RequestParam("offset") int offset){
        return new ReturnType<>(new ReturnListType<Contest>(contestDao.listAllContest(offset,limit),contestDao.getNum()));
    }

    @RequestMapping("/api/contest/problem")
    public ReturnType<Object> listProblems(@RequestParam(value = "contest_id",required = false) Integer contest_id,
                                           @RequestParam(value = "problem_id",required = false) String problem_id, HttpServletRequest request){
        User user= Authentication.getUser(request);
        if(problem_id==null){
            var res=contestDao.getProblemsID(contest_id);
            if(res==null){
                return new ReturnType<>("error","No such contest");
            }
            List<Problem> problems=new ArrayList<>();
            for(int i=0;i<res.size();i++){
                var p=problemDao.getProblem(res.get(i));
                p.set_id(String.valueOf((char)('A'+i)));
                if(user!=null){
                    if(problemDao.ACinContest(user.getId(),p.getId(),contest_id)>0){
                        p.setMy_status(0);
                    }
                    else if(problemDao.Was(user.getId(),p.getId(),null,contest_id)>0){
                        p.setMy_status(-2);
                    }
                }
                problems.add(p);
            }
            return new ReturnType<>(problems);
        }
        else{
            int num=problem_id.charAt(0)-'A';
            var res=problemDao.getProblemInContest(contest_id,num);
            if(res==null){
                return new ReturnType<>("error","No such problem");
            }
            res.setTime_limit(res.getTime_limit()*1000);
//            res.setId();
            res.set_id(problem_id);
            return new ReturnType<>(res);
        }
    }

    @RequestMapping("/api/contest")
    public ReturnType<Contest> getContest(@RequestParam("id") int id){
        return new ReturnType<>(contestDao.getContest(id));
    }


}
