package io.hyperfoil.report;

import org.aesh.AeshRuntimeRunner;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.impl.parser.CommandLineParser;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;
import io.hyperfoil.tools.parse.factory.CsvFactory;
import io.hyperfoil.tools.parse.file.FileRule;
import io.hyperfoil.tools.parse.file.RuleBuilder;
import io.hyperfoil.tools.yaup.AsciiArt;
import io.hyperfoil.tools.yaup.file.FileUtility;
import io.hyperfoil.tools.yaup.json.Json;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@CommandDefinition(name="generate-report", description = "generate a report from Hyperfoil output files")
public class ReportGenerator implements Command {

   public static final String DEFAULT_TOKEN = "[/**DATAKEY**/]";

   public static final String PHASE = "(?<phase>[^";

   public static List<FileRule> rules(){
      String phaseIterFork = "phase.${{phase}}.iteration.${{iteration:all}}.fork.${{fork:all}}";
      String phaseIterForkMetric = "phase.${{phase}}.iteration.${{iteration:all}}.fork.${{fork:all}}.metric.${{statistic}}";

      return Arrays.asList(
         new RuleBuilder("info.json")
            .path("info.json$")
            .nest("info")
            .asJson(),
         new RuleBuilder("total.csv")
            .path("total.csv$")
            .nest("total")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("failures.csv")
            .path("failures.csv$")
            .nest("failures")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase.session.csv")
            .path(".*?/(?<phase>[^/\\._]+)\\.sessions\\.csv$")
            .nest(phaseIterFork+".sessions")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase_iterartion_fork.session.csv")
            .path(".*?/(?<phase>[^/\\._]+)_(?<iteration>\\d*)[_]?(?<fork>[^\\./]+)\\.sessions\\.csv$")
            .nest(phaseIterFork+".sessions")

            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase.statistic.stepId.histogram.csv")
            .path(".*?/(?<phase>[^/\\._]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.histogram\\.csv$")
            .nest(phaseIterForkMetric+".histogram")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase_iteration_fork.statistic.stepId.histogram.csv")
            .path(".*?/(?<phase>[^/\\._]+)[_\\.](?<iteration>\\d*)[_]?(?<fork>[^\\.]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.histogram\\.csv$")
            .nest(phaseIterForkMetric+".histogram")
            .asText(new CsvFactory()::newParser),

         new RuleBuilder("phase_iteration_fork.statistic.stepId.series.csv")
            .path(".*?/(?<phase>[^/\\._]+)_(?<iteration>\\d*)[_]?(?<fork>[^\\.]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.series\\.csv$")
            .nest(phaseIterForkMetric+".series")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase.statistic.stepId.series.csv")
            .path(".*?/(?<phase>[^/\\._]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.series\\.csv$")
            .nest(phaseIterForkMetric+".series")
            .asText(new CsvFactory()::newParser),

         new RuleBuilder("phase_iteration_fork.statistic.stepId.agent.histogram.csv")
            .path(".*?/(?<phase>[^/\\._]+)_(?<iteration>\\d*)[_]?(?<fork>[^\\.]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.agent\\.(?<agentId>.{8}-.{4}-.{4}-.{4}-.{12})\\.histogram\\.csv$")
            .nest("agent.${{agentId}}."+phaseIterForkMetric+".histogram")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase.statistic.stepId.agent.histogram.csv")
            .path(".*?/(?<phase>[^/\\._]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.agent\\.(?<agentId>.{8}-.{4}-.{4}-.{4}-.{12})\\.histogram\\.csv$")
            .nest("agent.${{agentId}}."+phaseIterForkMetric+".histogram")
            .asText(new CsvFactory()::newParser),

         new RuleBuilder("phase_iteration_fork.statistic.stepId.agent.series.csv")
            .path(".*?/(?<phase>[^/\\._]+)_(?<iteration>\\d*)[_]?(?<fork>[^\\.]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.agent\\.(?<agentId>.{8}-.{4}-.{4}-.{4}-.{12})\\.series\\.csv$")
            .nest("agent.${{agentId}}."+phaseIterForkMetric+".series")
            .asText(new CsvFactory()::newParser),
         new RuleBuilder("phase.statistic.stepId.agent.series.csv")
            .path(".*?/(?<phase>[^/\\._]+)\\.(?<statistic>[^\\.]+)\\.(?<stepId>\\d+)\\.agent\\.(?<agentId>.{8}-.{4}-.{4}-.{4}-.{12})\\.series\\.csv$")
            .nest("agent.${{agentId}}."+phaseIterForkMetric+".series")
            .asText(new CsvFactory()::newParser),

         new RuleBuilder("agent.total")
            .path(".*?/agent\\.(?<agentId>.{8}-.{4}-.{4}-.{4}-.{12})\\.csv$")
            .nest("agent.${{agentId}}.total")
            .asText(new CsvFactory()::newParser)
      );
   }

   @Option(shortName = 's', required = true, description = "source folder or archive containing hyperfoil output files")
   private String source;

   @Option(shortName = 'd', required = true, description = "destination for output report file")
   private String destination;

   @Option(shortName = 't', required = false, description = "use a custom html template file", defaultValue = "")
   private String template;

   @Option(shortName = 'x', required = false, description = "replace token in html template file",defaultValue = DEFAULT_TOKEN)
   private String token;


   public static void main(String[] args) {
      AeshRuntimeRunner.builder().command(ReportGenerator.class).args(args).execute();
   }

   @Override
   public CommandResult execute(CommandInvocation commandInvocation) {

      if(template!=null && !template.isEmpty()){
         if( !(new File(template).exists())){
            template = "";//if the teample does not exist we error
            System.out.println("Could not find template = "+template);
            return CommandResult.FAILURE;
         }
      }
      if( (template==null || template.isEmpty() ) && !DEFAULT_TOKEN.equals(token)){
         System.out.println("A token should only be defined with a valid template file");
         return CommandResult.FAILURE;
      }
      List<FileRule> fileRules = rules();

      List<String> missed = new ArrayList<>();
      Json result = new Json();
      List<String> entries = FileUtility.isArchive(getSource()) ?
         FileUtility.getArchiveEntries(getSource()).stream().map(entry -> getSource() + FileUtility.ARCHIVE_KEY + entry).collect(Collectors.toList()) :
         FileUtility.getFiles(getSource(),"",true);
      for (String entry : entries) {
         AtomicBoolean matched = new AtomicBoolean(false);
         for (FileRule rule : fileRules) {

            rule.apply(entry, (nest, json) -> {
               matched.set(true);
               Json.chainMerge(result, nest, json);
            });
         }
         if(!matched.get()){
            missed.add(entry);

         }
      }
      if(!missed.isEmpty()){
         System.out.println(AsciiArt.ANSI_RED+"ignored:"+AsciiArt.ANSI_RESET);
         missed.forEach(whoops->{
            System.out.println("  "+whoops);
         });
      }
//      //Creates an index.html to use when developing the report view
//      try {
//         String template = new String(Files.readAllBytes(Paths.get("/home/wreicher/code/local/Hyperfoil-report/src/main/node/public/index.template.html")));
//         template = template.replace("[/**DATAKEY**/]",result.toString());
//         Files.write(Paths.get("/home/wreicher/code/local/Hyperfoil-report/src/main/node/public/index.html"),template.getBytes());
//      } catch (IOException e) {
//         e.printStackTrace();
//      }

      try (
         BufferedReader reader =
            new BufferedReader(
               new InputStreamReader(
                  (template!=null && !template.isEmpty()) ? new FileInputStream(template) : ReportGenerator.class.getClassLoader().getResourceAsStream("index.html")
               )
            )
      ) {
         String indexHtml = reader.lines().collect(Collectors.joining("\n"));
         indexHtml = indexHtml.replace("[/**DATAKEY**/]",result.toString());
         Files.write(Paths.get(getDestination()),indexHtml.getBytes());
         //Files.write()
         Files.write(Paths.get("/tmp/result.json"),result.toString().getBytes());

      } catch (FileNotFoundException e) {
         e.printStackTrace();
         return CommandResult.SUCCESS;
      } catch (IOException e) {
         e.printStackTrace();
         return CommandResult.SUCCESS;
      }


      //System.out.println(result.toString(2));

      return CommandResult.SUCCESS;
   }

   public String getToken() {
      return token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public String getTemplate() {
      return template;
   }

   public void setTemplate(String template) {
      this.template = template;
   }

   public String getDestination() {
      return destination;
   }

   public void setDestination(String destination) {
      this.destination = destination;
   }

   public void setSource(String source) {
      this.source = source;
   }
   public String getSource(){return source;}
}
