# Hyperfoil reports
Generate html reports from the Hyperfoil output csv files. The report is a self contained html file with embedded javascript, css, and run data. 
Reports can easily grow to several MB.
**NOTE** The reports do not embed the patternfly4 fonts. This results in several 404's when opening a report and is being discussed

```
java -cp... -jar report.jar -s /tmp/Hyperfoil/reports/0001 -d /tmp/index.html
```

## Building
```
mvn clean install
```
The report tool consists of a java class to compile the Hyperfoil csv files into json and a nodejs project to create the html template.
`mvn clean install` will build the nodejs project and embed the javascript and css into a index.html template in `src/main/resources`
and in the resulting jar.

## Developing
The node project is based on `create-react-app` and uses `yarn` for package management. 
This means you need to download node and add it to the path then install yarn with `npm install -g yarn` if you do not already have them 

run `yarn install` from the `src/main/node` directory for first time setup. Use `yarn start` to launch the react hot reload server but 
first create an index.html in `src/main/node/public` with data from a Hyperfoil run.
```
java -cp... -jar report.jar -s /tmp/Hyperfoil/reports/0001 -d ${PROJECT_HOME}/src/main/node/public/index.html 
``` 
This will embed the json data in the index.hml and the hot reload server will add the javascript and css 
