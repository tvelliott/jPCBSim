function CSX = pcb(CSX)
% matlab script created by hyp2mat
% create minimal mesh
mesh = {};
mesh.x = [0.00254 0.08941];
mesh.y = [0.00152 0.07696];
mesh.z = [0 0.00080518];
% add mesh
CSX = DefineRectGrid(CSX, 1, mesh);
CSX = AddHyperLynxDielectric(CSX, 'Dielectric_DL01', 4.8, 0.02);
% board outline, layer DL01
CSX = AddBox(CSX, 'Dielectric_DL01', 100, [ 0.00254, 0.00152, 0], [ 0.08941, 0.07696, 0.00080518]  );
% copper
% create layer Top material
CSX = AddMetal(CSX, 'Top_copper'); % perfect conductor
% create layer Top cutout
CSX = AddMaterial( CSX, 'Top_cutout');
CSX = SetMaterialProperty( CSX, 'Top_cutout', 'Epsilon', 1, 'Mue', 1);
% copper
pgon = [];
pgon(:, end+1) = [0.07722;0.03632];
pgon(:, end+1) = [0.07722;0.07163];
pgon(:, end+1) = [0.08331;0.07163];
pgon(:, end+1) = [0.08331;0.0475];
pgon(:, end+1) = [0.08052;0.0475];
pgon(:, end+1) = [0.08052;0.03886];
pgon(:, end+1) = [0.08687;0.03886];
pgon(:, end+1) = [0.08687;0.0475];
pgon(:, end+1) = [0.08433;0.0475];
pgon(:, end+1) = [0.08433;0.07264];
pgon(:, end+1) = [0.02718;0.07264];
pgon(:, end+1) = [0.02718;0.07163];
pgon(:, end+1) = [0.0762;0.07163];
pgon(:, end+1) = [0.0762;0.03632];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.0729;0.03886];
pgon(:, end+1) = [0.0729;0.0475];
pgon(:, end+1) = [0.00406;0.0475];
pgon(:, end+1) = [0.00406;0.03886];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.07341;0.00202];
pgon(:, end+1) = [0.07342;0.00203];
pgon(:, end+1) = [0.07342;0.00406];
pgon(:, end+1) = [0.07341;0.00407];
pgon(:, end+1) = [0.07341;0.00408];
pgon(:, end+1) = [0.07264;0.00408];
pgon(:, end+1) = [0.07264;0.00407];
pgon(:, end+1) = [0.07263;0.00406];
pgon(:, end+1) = [0.07263;0.00203];
pgon(:, end+1) = [0.07264;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07266;0.00204];
pgon(:, end+1) = [0.07266;0.00405];
pgon(:, end+1) = [0.07339;0.00405];
pgon(:, end+1) = [0.07339;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.07519;0.00202];
pgon(:, end+1) = [0.0752;0.00203];
pgon(:, end+1) = [0.0752;0.00406];
pgon(:, end+1) = [0.07518;0.00408];
pgon(:, end+1) = [0.07442;0.00408];
pgon(:, end+1) = [0.07441;0.00407];
pgon(:, end+1) = [0.07441;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07443;0.00204];
pgon(:, end+1) = [0.07443;0.00405];
pgon(:, end+1) = [0.07517;0.00405];
pgon(:, end+1) = [0.07517;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.07697;0.00202];
pgon(:, end+1) = [0.07697;0.00407];
pgon(:, end+1) = [0.07696;0.00408];
pgon(:, end+1) = [0.0762;0.00408];
pgon(:, end+1) = [0.07619;0.00407];
pgon(:, end+1) = [0.07619;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07621;0.00204];
pgon(:, end+1) = [0.07621;0.00405];
pgon(:, end+1) = [0.07695;0.00405];
pgon(:, end+1) = [0.07695;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.07875;0.00202];
pgon(:, end+1) = [0.07875;0.00407];
pgon(:, end+1) = [0.07874;0.00408];
pgon(:, end+1) = [0.07798;0.00408];
pgon(:, end+1) = [0.07797;0.00407];
pgon(:, end+1) = [0.07797;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07799;0.00204];
pgon(:, end+1) = [0.07799;0.00405];
pgon(:, end+1) = [0.07873;0.00405];
pgon(:, end+1) = [0.07873;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.08053;0.00202];
pgon(:, end+1) = [0.08053;0.00407];
pgon(:, end+1) = [0.08052;0.00408];
pgon(:, end+1) = [0.07976;0.00408];
pgon(:, end+1) = [0.07974;0.00406];
pgon(:, end+1) = [0.07974;0.00203];
pgon(:, end+1) = [0.07975;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07977;0.00204];
pgon(:, end+1) = [0.07977;0.00405];
pgon(:, end+1) = [0.08051;0.00405];
pgon(:, end+1) = [0.08051;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.0823;0.00202];
pgon(:, end+1) = [0.08231;0.00203];
pgon(:, end+1) = [0.08231;0.00406];
pgon(:, end+1) = [0.0823;0.00407];
pgon(:, end+1) = [0.0823;0.00408];
pgon(:, end+1) = [0.08153;0.00408];
pgon(:, end+1) = [0.08153;0.00407];
pgon(:, end+1) = [0.08152;0.00406];
pgon(:, end+1) = [0.08152;0.00203];
pgon(:, end+1) = [0.08153;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.08155;0.00204];
pgon(:, end+1) = [0.08155;0.00405];
pgon(:, end+1) = [0.08228;0.00405];
pgon(:, end+1) = [0.08228;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.08408;0.00202];
pgon(:, end+1) = [0.08409;0.00203];
pgon(:, end+1) = [0.08409;0.00406];
pgon(:, end+1) = [0.08407;0.00408];
pgon(:, end+1) = [0.08331;0.00408];
pgon(:, end+1) = [0.0833;0.00407];
pgon(:, end+1) = [0.0833;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.08332;0.00204];
pgon(:, end+1) = [0.08332;0.00405];
pgon(:, end+1) = [0.08406;0.00405];
pgon(:, end+1) = [0.08406;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.08586;0.00202];
pgon(:, end+1) = [0.08586;0.00407];
pgon(:, end+1) = [0.08585;0.00408];
pgon(:, end+1) = [0.08509;0.00408];
pgon(:, end+1) = [0.08508;0.00407];
pgon(:, end+1) = [0.08508;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.0851;0.00204];
pgon(:, end+1) = [0.0851;0.00405];
pgon(:, end+1) = [0.08584;0.00405];
pgon(:, end+1) = [0.08584;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% copper
pgon = [];
pgon(:, end+1) = [0.07164;0.00202];
pgon(:, end+1) = [0.07164;0.00407];
pgon(:, end+1) = [0.07163;0.00408];
pgon(:, end+1) = [0.07087;0.00408];
pgon(:, end+1) = [0.07085;0.00406];
pgon(:, end+1) = [0.07085;0.00203];
pgon(:, end+1) = [0.07086;0.00202];
CSX = AddPolygon(CSX, 'Top_copper', 200, 2, 0.00080518, pgon);
% cutout
pgon = [];
pgon(:, end+1) = [0.07088;0.00204];
pgon(:, end+1) = [0.07088;0.00405];
pgon(:, end+1) = [0.07162;0.00405];
pgon(:, end+1) = [0.07162;0.00204];
CSX = AddPolygon(CSX, 'Top_cutout', 201, 2, 0.00080518, pgon);
% create layer Bottom material
CSX = AddMetal(CSX, 'Bottom_copper'); % perfect conductor
% copper
pgon = [];
pgon(:, end+1) = [0.08687;0.00432];
pgon(:, end+1) = [0.08687;0.0475];
pgon(:, end+1) = [0.07798;0.0475];
pgon(:, end+1) = [0.07798;0.06452];
pgon(:, end+1) = [0.07544;0.06452];
pgon(:, end+1) = [0.07544;0.0475];
pgon(:, end+1) = [0.00406;0.0475];
pgon(:, end+1) = [0.00406;0.00432];
CSX = AddPolygon(CSX, 'Bottom_copper', 200, 2, 0, pgon);
% via copper
CSX = AddMetal( CSX, 'via' );
CSX = AddCylinder(CSX, 'via', 300, [ 0.084836 , 0.042418 , 0 ], [ 0.084836 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.068326 , 0.042418 , 0 ], [ 0.068326 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.046736 , 0.042418 , 0 ], [ 0.046736 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.020828 , 0.042418 , 0 ], [ 0.020828 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.034036 , 0.042418 , 0 ], [ 0.034036 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.057658 , 0.042418 , 0 ], [ 0.057658 , 0.042418 , 0.00080518 ], 0.001016);
CSX = AddCylinder(CSX, 'via', 300, [ 0.007874 , 0.042418 , 0 ], [ 0.007874 , 0.042418 , 0.00080518 ], 0.001016);
% devices
CSX.HyperLynxDevice = {};
CSX.HyperLynxDevice{end+1} = struct('name', 'TPTP10SQ', 'ref', 'TP1', 'layer_name', 'Top');
% ports
CSX.HyperLynxPort = {};
CSX.HyperLynxPort{end+1} = struct('ref', 'TP1.TP', 'xc', 0.07671, 'yc', 0.03683, 'z', 0.00080518, 'x1', 0.0762, 'y1', 0.03632, 'x2', 0.07722, 'y2', 0.03734, 'position', 'top', 'layer_name', 'Top');
%not truncated
