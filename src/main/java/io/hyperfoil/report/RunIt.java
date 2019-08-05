package io.hyperfoil.report;

import org.aesh.AeshRuntimeRunner;

public class RunIt {
   public static void main(String[] args) {
      args = new String[]{
         "-s",
         //"/home/wreicher/perfWork/Hyperfoil/00A0",
         //"/home/wreicher/perfWork/Hyperfoil/johara/0002",
         "/home/wreicher/perfWork/Hyperfoil/rvansa/00B9",
         //"/home/wreicher/perfWork/Hyperfoil/rvansa/test",
         "-t",
         "/home/wreicher/code/local/Hyperfoil-report/src/main/node/public/index.template.html",
         "-d",
         "/home/wreicher/code/local/Hyperfoil-report/src/main/node/public/index.html"
      };
      AeshRuntimeRunner.builder().command(ReportGenerator.class).args(args).execute();
   }
}
